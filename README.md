# PSM-VanchanShravan-Yagna

Angular + Spring Boot + PostgreSQL app for booking Vanchan / Shravan Yagna.

## Features

- **Vanchan Yagna** – a haribhakt picks a date and gets the next **15 consecutive pages** (never more) of one book, and the app shows which book and which page numbers to read. Books are read in order and a booking never spans two books (the last booking of a book can be shorter). One booking per haribhakt per date, any number of dates. Page ranges are handed out in sequence across *all* haribhakts and are never reused. The haribhakt marks the booking **Completed** after reading.
- **Group Vanchan Yagna** – pick a group name from a list, add the haribhakt names, create the group, and mark it **Completed** when the vanchan is done.
- **Shravan Yagna** – a haribhakt picks a date and gets the next **1 audio track**. One booking per haribhakt per date, any number of dates. Tracks are handed out in sequence and never reused. Mark **Completed** after listening.

Allocation is serialized with a row lock and protected by unique constraints (`start_page`, `track_id`, `haribhakt + date`), so concurrent bookings can never receive the same pages or track.

Haribhakts identify themselves with name + mandal + BAPS ID (no password); the BAPS ID is the unique key (3–20 characters, no spaces, stored upper-case). The mandal is free text.

## Run locally

```powershell
docker compose up -d                      # PostgreSQL on :5432 (db psm_yagna, postgres/postgres)
cd backend;  .\mvnw.cmd spring-boot:run   # API on :8080
cd frontend; npm install; npm start       # UI on :4200
```

## Progressive Web App

The UI is an installable PWA (web manifest + Angular service worker; icons in `frontend/public/icons`). The service worker is only active in production builds, and installing needs HTTPS (or `localhost`):

```powershell
cd frontend; npm run build    # output in dist/frontend/browser
```

Serve that folder from a web server that also proxies `/api` to the backend (the production build calls `/api`), then use the browser's *Install app* option. Only the app shell and images are cached; bookings still need a connection.

## Deploy on Render

[render.yaml](render.yaml) is a Render Blueprint with two services: `vanchan-yagna-backend` (Docker, Spring Boot) and `vanchan-yagna` (static Angular PWA). The backend uses the existing production database `parasabha_planner` and keeps its tables in its own schema, `vanchan-yagna`, which it creates on first start (`DB_SCHEMA`).

1. In Render choose **New > Blueprint**, connect this repo and apply `render.yaml`.
2. When prompted, fill `DB_HOST`, `DB_USERNAME` and `DB_PASSWORD` from the `parasabha-planner-db` *Internal Database URL* (same region as the backend, `oregon`).
3. If Render gives the services different URLs, update `CORS_ALLOWED_ORIGINS` (backend) and `API_BASE_URL` (frontend, then redeploy it).
4. Add the two static-site headers listed at the bottom of `render.yaml` (Blueprints can't set them).

Backend settings (env vars): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_SCHEMA` (default `public`), `DB_USERNAME`, `DB_PASSWORD`, `PORT`, `CORS_ALLOWED_ORIGINS`.

## Configuration to fill in

Edit [application.properties](backend/src/main/resources/application.properties):

| Property | Meaning |
|---|---|
| `app.vanchan.pages-per-booking` | max pages per booking (15), always taken from a single book |
| `app.vanchan.books-file` | books to read (`title\|total pages` per line, in reading order), loaded on first start — currently from the *વાંચન યજ્ઞ* sheet |
| `app.group-vanchan.names` | placeholder group names, only seeded into an empty `group_name` table (afterwards the dropdown follows that table) |
| `app.shravan.tracks-file` | audio track titles (one per line, in listening order), loaded into an empty `shravan_track` table — currently the 614 tracks of the *શ્રવણ યજ્ઞ* sheet. Only tracks in that table are ever handed out |

Add audio URLs or more tracks with
`POST /api/shravan/tracks` (`{"title": "...", "audioUrl": "https://..."}`) — new tracks are appended to the end of the order — or by editing the `shravan_track` table.

## Tests

```powershell
cd backend; .\mvnw.cmd test    # uses in-memory H2, no PostgreSQL needed
```
