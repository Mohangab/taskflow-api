/**
 * Production / default environment.
 *
 * Local API: http://localhost:8080
 *
 * Production tip — serve the Angular build via nginx (or Spring static)
 * and proxy /api to the backend so the browser uses same-origin requests:
 *
 *   location /api/ {
 *     proxy_pass http://app:8080/api/;
 *   }
 *
 * Then set apiUrl to '' (empty) so HttpClient calls relative paths like /api/tasks.
 */
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080',
};
