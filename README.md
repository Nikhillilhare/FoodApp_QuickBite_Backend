🍔 QuickBite — Food Delivery Platform
QuickBite is a full-stack food-delivery platform with a customer-facing
ordering app and an admin panel to manage the whole business, backed by a
single Spring Boot REST API.

This is a portfolio/project-overview README. It ties together three
separate codebases — use it as the top-level README if you keep them in
one repo, or as a pinned "QuickBite" repo that links out to the three
individual repos.

🧩 Sub-projects
Project	Stack	Default port	README
Backend API	Spring Boot, MySQL, Flyway, JWT	8080	backend README
Admin Panel	React, Vite, Tailwind v4, Redux Toolkit	3000	admin-frontend README
Customer App	React, Vite, Tailwind v4, Redux Toolkit	3001	customer-frontend README
Both frontends talk to the same backend at http://localhost:8080 (or a
deployed URL, via VITE_API_BASE_URL).

✨ Features
Customer app

Signup/login with email OTP verification, forgot/reset password
Browse menu by category, cart, checkout with delivery address
Payment method selection (UPI / Card / Net Banking / Cash)
Order history & live order status tracking
Profile & saved addresses
Admin panel

Dashboard: today's sales, orders, customers, pending orders, recent orders
Order management: filter by status, update status, cancel
Menu & category CRUD, inventory (stock purchase/adjust/expiry)
Customer list with lifetime stats
Payments: filter by status, mark success/failed/refund
Reports: sales/billing/stock, top items, payment method & city breakdown,
daily sales chart (This Week / This Month / Last 3 Months / This Year)
Settings: restaurant/delivery/payment/notification config, plus a Security
tab (change password, two-factor authentication)
🏗️ Architecture
┌────────────────────┐        ┌────────────────────┐
│   Admin Panel       │        │   Customer App       │
│   React (port 3000) │        │   React (port 3001)  │
└──────────┬──────────┘        └──────────┬──────────┘
           │        REST + JWT (Axios)     │
           └───────────────┬───────────────┘
                            ▼
               ┌─────────────────────────┐
               │   Spring Boot API        │
               │   (port 8080)             │
               │   /api/auth/**             │
               │   /api/admin/**            │
               │   /api/customer/**         │
               └────────────┬─────────────┘
                            ▼
                   ┌─────────────────┐
                   │  MySQL (Flyway)  │
                   └─────────────────┘
🚀 Quick start (local)
Backend
cd backend
./mvnw spring-boot:run
Admin panel
cd admin-frontend
npm install
cp .env.example .env
npm run dev # http://localhost:3000
Customer app
cd customer-frontend
npm install
npm run dev # http://localhost:3001
See each sub-project's own README for full setup, environment variables,
and deployment notes.

🔐 Security
Secrets (DB password, JWT secret, mail credentials) ,
JWT-based auth with short-lived access tokens + refresh tokens.
CORS is restricted via FRONTEND_ORIGIN to only the configured frontend
URLs.
📦 Tech stack
Backend: Java 21, Spring Boot (Web, Security, Data JPA, Mail),
MySQL, Flyway, JWT, springdoc-openapi (Swagger UI)
Frontends: React 19, Vite, Tailwind CSS v4, Redux Toolkit,
React Router v7, Axios, Recharts (admin reports), react-hot-toast
🌐 Deployment
Not yet deployed — see the roadmap:

Backend → Render / Railway (with a managed MySQL add-on)
Frontends → Vercel / Netlify
Once deployed, update the badges/links below with the live URLs.
👤 Author
Built by Nikhil Lilhare.
