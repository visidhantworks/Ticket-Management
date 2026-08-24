# Ticket Management Frontend

Single-login role-based frontend for the provided Spring Boot Ticket Management backend.

## Run

1. Start the Spring Boot backend on `http://localhost:8080`.
2. Start this frontend from its folder with a static server, for example:

```bash
python3 -m http.server 5500
```

3. Open `http://localhost:5500`.

The backend CORS configuration already allows ports 5500 and 127.0.0.1:5500.

## Login

- Requestor: `requestor@test.com` / `Requestor@123`
- Support: `support@test.com` / `Support@123`

The frontend sends credentials to `/api/auth/login`, stores the returned JWT and role, and automatically opens the correct dashboard.

## API base URL

If your backend is not running on port 8080, edit `API_BASE` at the top of `js/app.js`.
