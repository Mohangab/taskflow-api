import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TaskService } from '../../../core/services/task.service';
import {
  STATUS_LABELS,
  TASK_STATUSES,
  Task,
  TaskStatus,
} from '../../../core/models/task.model';
import { ApiError } from '../../../core/models/error.model';

type FilterStatus = TaskStatus | 'ALL';

@Component({
  selector: 'app-task-board',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './task-board.component.html',
  styleUrl: './task-board.component.scss',
})
export class TaskBoardComponent implements OnInit {
  private readonly tasksApi = inject(TaskService);
  private readonly fb = inject(FormBuilder);

  readonly statuses = TASK_STATUSES;
  readonly statusLabels = STATUS_LABELS;
  readonly filters: { value: FilterStatus; label: string }[] = [
    { value: 'ALL', label: 'All' },
    { value: 'TODO', label: 'To Do' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'DONE', label: 'Done' },
  ];

  tasks: Task[] = [];
  filter: FilterStatus = 'ALL';
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  showForm = false;
  editingId: number | null = null;

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', [Validators.maxLength(2000)]],
    status: this.fb.nonNullable.control<TaskStatus>('TODO'),
  });

  ngOnInit(): void {
    this.loadTasks();
  }

  get filteredTasks(): Task[] {
    if (this.filter === 'ALL') {
      return this.tasks;
    }
    return this.tasks.filter((t) => t.status === this.filter);
  }

  get counts(): Record<TaskStatus | 'ALL', number> {
    return {
      ALL: this.tasks.length,
      TODO: this.tasks.filter((t) => t.status === 'TODO').length,
      IN_PROGRESS: this.tasks.filter((t) => t.status === 'IN_PROGRESS').length,
      DONE: this.tasks.filter((t) => t.status === 'DONE').length,
    };
  }

  loadTasks(): void {
    this.loading = true;
    this.errorMessage = '';
    this.tasksApi.list().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = this.extractError(err, 'Failed to load tasks.');
      },
    });
  }

  setFilter(value: FilterStatus): void {
    this.filter = value;
  }

  openCreate(): void {
    this.editingId = null;
    this.form.reset({ title: '', description: '', status: 'TODO' });
    this.showForm = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  openEdit(task: Task): void {
    this.editingId = task.id;
    this.form.setValue({
      title: task.title,
      description: task.description ?? '',
      status: task.status,
    });
    this.showForm = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const payload = {
      title: raw.title.trim(),
      description: raw.description.trim() || null,
      status: raw.status,
    };
    this.saving = true;
    this.errorMessage = '';
    const isCreate = this.editingId == null;

    const request$ = isCreate
      ? this.tasksApi.create(payload)
      : this.tasksApi.update(this.editingId!, payload);

    request$.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.editingId = null;
        this.successMessage = isCreate ? 'Task created.' : 'Task updated.';
        this.loadTasks();
      },
      error: (err: HttpErrorResponse) => {
        this.saving = false;
        this.errorMessage = this.extractError(err, 'Could not save task.');
      },
    });
  }

  changeStatus(task: Task, status: TaskStatus): void {
    if (task.status === status) {
      return;
    }
    this.tasksApi
      .update(task.id, {
        title: task.title,
        description: task.description,
        status,
      })
      .subscribe({
        next: (updated) => {
          this.tasks = this.tasks.map((t) => (t.id === updated.id ? updated : t));
          this.successMessage = `Moved “${updated.title}” to ${STATUS_LABELS[status]}.`;
        },
        error: (err: HttpErrorResponse) => {
          this.errorMessage = this.extractError(err, 'Could not update status.');
        },
      });
  }

  deleteTask(task: Task): void {
    if (!confirm(`Delete task “${task.title}”?`)) {
      return;
    }
    this.tasksApi.delete(task.id).subscribe({
      next: () => {
        this.tasks = this.tasks.filter((t) => t.id !== task.id);
        this.successMessage = 'Task deleted.';
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = this.extractError(err, 'Could not delete task.');
      },
    });
  }

  statusBadgeClass(status: TaskStatus): string {
    switch (status) {
      case 'TODO':
        return 'badge-todo';
      case 'IN_PROGRESS':
        return 'badge-progress';
      case 'DONE':
        return 'badge-done';
    }
  }

  private extractError(err: HttpErrorResponse, fallback: string): string {
    const body = err.error as ApiError | string | null;
    if (body && typeof body === 'object') {
      if (body.details?.length) {
        return body.details.join(' ');
      }
      if (body.message) {
        return body.message;
      }
    }
    if (err.status === 0) {
      return 'Cannot reach the API. Is the Spring Boot server running on port 8080?';
    }
    return fallback;
  }
}
