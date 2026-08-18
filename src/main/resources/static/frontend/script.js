const API_BASE = "http://localhost:8080";

let token = localStorage.getItem("token");
let role = localStorage.getItem("role");
let email = localStorage.getItem("email");

const loginPage = document.getElementById("loginPage");
const appPage = document.getElementById("appPage");
const loginForm = document.getElementById("loginForm");
const loginError = document.getElementById("loginError");
const content = document.getElementById("content");
const pageTitle = document.getElementById("pageTitle");
const userRole = document.getElementById("userRole");
const userEmail = document.getElementById("userEmail");
const requestorNav = document.getElementById("requestorNav");
const supportNav = document.getElementById("supportNav");
const modal = document.getElementById("modal");
const modalContent = document.getElementById("modalContent");

function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function authHeaders() {
    const headers = {
        "Content-Type": "application/json"
    };

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    return headers;
}

async function apiRequest(path, options = {}) {
    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            ...authHeaders(),
            ...(options.headers || {})
        }
    });

    if (response.status === 401) {
        logout();
        throw new Error("Session expired. Please login again.");
    }

    if (response.status === 403) {
        throw new Error("You are not authorized to perform this action.");
    }

    const text = await response.text();

    let data = null;

    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!response.ok) {
        const message =
            data?.message ||
            data?.error ||
            "Request failed.";

        throw new Error(message);
    }

    return data;
}

 

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    loginError.textContent = "";

    const emailValue = document
        .getElementById("email")
        .value
        .trim();

    const password = document.getElementById("password").value;

    try {
        const response = await fetch(`${API_BASE}/api/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: emailValue,
                password: password
            })
        });

        const text = await response.text();

        let data;

        try {
            data = text ? JSON.parse(text) : null;
        } catch {
            throw new Error("Invalid response received from server.");
        }

        if (!response.ok) {
            throw new Error(
                data?.message ||
                data?.error ||
                "Login failed."
            );
        }

        token = data.token;
        role = data.role;
        email = emailValue;

        if (!token || !role) {
            throw new Error(
                "Login response did not contain token and role."
            );
        }

        localStorage.setItem("token", token);
        localStorage.setItem("role", role);
        localStorage.setItem("email", email);

        showApp();

    } catch (error) {
        loginError.textContent = error.message;
    }
});

function showApp() {
    loginPage.classList.add("hidden");
    appPage.classList.remove("hidden");

    userRole.textContent = role || "";
    userEmail.textContent = email || "";

    requestorNav.classList.add("hidden");
    supportNav.classList.add("hidden");

    if (role === "REQUESTOR") {
        requestorNav.classList.remove("hidden");
        loadView("myTickets");
        return;
    }

    if (role === "SUPPORT_ENGINEER") {
        supportNav.classList.remove("hidden");
        loadView("assignedTickets");
        return;
    }

    logout();
    alert("Unsupported user role.");
}

async function logout() {

    try {
        if (token) {
            await fetch(`${API_BASE}/api/auth/logout`, {
                method: "POST",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            });
        }
    } catch (error) {
        console.error("Logout request failed:", error);
    }

    localStorage.clear();

    token = null;
    role = null;
    email = null;

    appPage.classList.add("hidden");
    loginPage.classList.remove("hidden");
    loginForm.reset();
}

 

document.getElementById("logoutBtn").addEventListener("click", logout);

document.querySelectorAll(".nav-btn").forEach((button) => {
    button.addEventListener("click", () => {

        document
            .querySelectorAll(".nav-btn")
            .forEach((btn) => btn.classList.remove("active"));

        button.classList.add("active");

        loadView(button.dataset.view);
    });
});

function setLoading() {
    content.innerHTML = `
        <div class="loading">
            Loading...
        </div>
    `;
}

async function loadView(view) {
    setLoading();

    const titles = {
        myTickets: "My Tickets",
        createTicket: "Create Ticket",
        assignedTickets: "Assigned Tickets",
        openTickets: "Open Tickets"
    };

    pageTitle.textContent =
        titles[view] || "Dashboard";

    try {
        if (view === "myTickets") {
            await renderMyTickets();
        }

        if (view === "createTicket") {
            renderCreateTicket();
        }

        if (view === "assignedTickets") {
            await renderAssignedTickets();
        }

        if (view === "openTickets") {
            await renderOpenTickets();
        }

    } catch (error) {
        content.innerHTML = `
            <div class="empty">
                ${escapeHtml(error.message)}
            </div>
        `;
    }
}

 

async function renderMyTickets() {
    const tickets =
        await apiRequest("/api/tickets/my-tickets");

    if (!tickets || tickets.length === 0) {

        content.innerHTML = `
            <div class="section-header">
                <h3>My Tickets</h3>

                <button
                    class="primary-btn"
                    style="width:auto;margin:0"
                    onclick="renderCreateTicket()">
                    Create Ticket
                </button>
            </div>

            <div class="empty">
                You haven't raised any tickets yet.
            </div>
        `;

        return;
    }

    content.innerHTML = `
        <div class="section-header">
            <h3>My Tickets</h3>

            <button
                class="primary-btn"
                style="width:auto;margin:0"
                onclick="renderCreateTicket()">
                Create Ticket
            </button>
        </div>

        <div class="ticket-grid">
            ${tickets.map(ticketCard).join("")}
        </div>
    `;
}

function renderCreateTicket() {

    pageTitle.textContent = "Create Ticket";

    content.innerHTML = `
        <div class="form-card">

            <h3>Create a new ticket</h3>

            <p class="muted" style="margin-top:6px">
                Describe the issue and our support team will take care of it.
            </p>

            <form id="createTicketForm">

                <label for="bankingClientId">
                    Banking Client ID
                </label>

                <input
                    id="bankingClientId"
                    type="number"
                    value="1"
                    min="1"
                    required
                >

                <label for="ticketTitle">
                    Title
                </label>

                <input
                    id="ticketTitle"
                    maxlength="255"
                    required
                    placeholder="Unable to login"
                >

                <label for="ticketDescription">
                    Description
                </label>

                <textarea
                    id="ticketDescription"
                    required
                    placeholder="Describe your issue..."
                ></textarea>

                <div class="form-row">

                    <div>
                        <label for="ticketCategory">
                            Category
                        </label>

                        <select id="ticketCategory">

                            <option value="LOGIN">
                                LOGIN
                            </option>

                            <option value="PAYMENT">
                                PAYMENT
                            </option>

                            <option value="ACCOUNT">
                                ACCOUNT
                            </option>

                            <option value="TECHNICAL">
                                TECHNICAL
                            </option>

                            <option value="OTHER">
                                OTHER
                            </option>

                        </select>
                    </div>

                    <div>
                        <label for="ticketPriority">
                            Priority
                        </label>

                        <select id="ticketPriority">

                            <option value="LOW">
                                LOW
                            </option>

                            <option value="MEDIUM">
                                MEDIUM
                            </option>

                            <option value="HIGH">
                                HIGH
                            </option>

                        </select>
                    </div>

                </div>

                <label for="ticketAttachment">
                    Attachment URL
                    <span class="muted">(optional)</span>
                </label>

                <input
                    id="ticketAttachment"
                    placeholder="https://..."
                >

                <button
                    class="primary-btn"
                    type="submit">
                    Create Ticket
                </button>

                <p
                    id="createError"
                    class="error">
                </p>

            </form>

        </div>
    `;

    document
        .getElementById("createTicketForm")
        .addEventListener("submit", createTicket);
}

async function createTicket(event) {
    event.preventDefault();

    const errorElement =
        document.getElementById("createError");

    errorElement.textContent = "";

    try {

        const ticket = await apiRequest(
            "/api/tickets",
            {
                method: "POST",

                body: JSON.stringify({
                    bankingClientId: Number(
                        document.getElementById(
                            "bankingClientId"
                        ).value
                    ),

                    title: document
                        .getElementById("ticketTitle")
                        .value
                        .trim(),

                    description: document
                        .getElementById("ticketDescription")
                        .value
                        .trim(),

                    category: document
                        .getElementById("ticketCategory")
                        .value,

                    priority: document
                        .getElementById("ticketPriority")
                        .value,

                    attachment:
                        document
                            .getElementById("ticketAttachment")
                            .value
                            .trim() || null
                })
            }
        );

        alert(
            `Ticket #${ticket.id} created successfully.`
        );

        loadView("myTickets");

    } catch (error) {
        errorElement.textContent =
            error.message;
    }
}

 

async function renderAssignedTickets() {

    const tickets =
        await apiRequest("/api/support/tickets");

    content.innerHTML = `
        <div class="section-header">
            <h3>Assigned Tickets</h3>
        </div>

        ${
            tickets && tickets.length
                ? `
                    <div class="ticket-grid">
                        ${tickets.map(ticketCard).join("")}
                    </div>
                  `
                : `
                    <div class="empty">
                        No tickets are currently assigned to you.
                    </div>
                  `
        }
    `;
}

async function renderOpenTickets() {

    const tickets =
        await apiRequest("/api/support/tickets/open");

    content.innerHTML = `
        <div class="section-header">
            <h3>Open Tickets</h3>
        </div>

        ${
            tickets && tickets.length
                ? `
                    <div class="ticket-grid">
                        ${tickets.map(ticketCard).join("")}
                    </div>
                  `
                : `
                    <div class="empty">
                        No open tickets found.
                    </div>
                  `
        }
    `;
}

/* =========================
   TICKET CARD
========================= */

function ticketCard(ticket) {

    const statusClass =
        `status-${String(ticket.status)
            .toLowerCase()
            .replaceAll("_", "-")}`;

    const priorityClass =
        `priority-${String(ticket.priority)
            .toLowerCase()}`;

    return `
        <article class="ticket-card">

            <div class="ticket-meta">

                <span class="badge ${statusClass}">
                    ${escapeHtml(ticket.status)}
                </span>

                <span class="badge ${priorityClass}">
                    ${escapeHtml(ticket.priority)}
                </span>

            </div>

            <h3>
                ${escapeHtml(ticket.title)}
            </h3>

            <p>
                ${escapeHtml(ticket.description)}
            </p>

            <div class="ticket-meta">

                <span class="muted">
                    #${ticket.id}
                </span>

                <span class="muted">
                    ${escapeHtml(ticket.category)}
                </span>

                <span class="muted">
                    ${escapeHtml(ticket.bankingClient)}
                </span>

            </div>

            <div class="card-actions">

                <button
                    class="secondary-btn"
                    onclick="showTicketDetails(${ticket.id})">
                    Details
                </button>

                ${
                    role === "SUPPORT_ENGINEER"
                        ? `
                            <button
                                class="secondary-btn"
                                onclick="showHistory(${ticket.id})">
                                History
                            </button>
                          `
                        : ""
                }

            </div>

        </article>
    `;
}

/* =========================
   TICKET DETAILS
========================= */

async function showTicketDetails(ticketId) {

    try {

        const endpoint =
            role === "REQUESTOR"
                ? `/api/tickets/${ticketId}`
                : `/api/support/tickets/${ticketId}`;

        const ticket =
            await apiRequest(endpoint);

        let comments = [];

        /*
         * Requestor comment endpoint may be different or unavailable.
         * If it fails, ticket details still work.
         */
        try {

            comments =
                await apiRequest(
                    `/api/tickets/${ticketId}/comments`
                );

        } catch {
            comments = [];
        }

        modalContent.innerHTML = `

            <h2>
                ${escapeHtml(ticket.title)}
            </h2>

            <p
                class="muted"
                style="margin-top:5px">
                Ticket #${ticket.id}
            </p>

            <div class="detail-grid">

                <div class="detail-item">
                    <small>Status</small>
                    <strong>
                        ${escapeHtml(ticket.status)}
                    </strong>
                </div>

                <div class="detail-item">
                    <small>Priority</small>
                    <strong>
                        ${escapeHtml(ticket.priority)}
                    </strong>
                </div>

                <div class="detail-item">
                    <small>Category</small>
                    <strong>
                        ${escapeHtml(ticket.category)}
                    </strong>
                </div>

                <div class="detail-item">
                    <small>Banking Client</small>
                    <strong>
                        ${escapeHtml(ticket.bankingClient)}
                    </strong>
                </div>

            </div>

            <div class="detail-card">

                <h3>
                    Description
                </h3>

                <p
                    style="margin-top:10px;line-height:1.6">
                    ${escapeHtml(ticket.description)}
                </p>

            </div>

            <div style="margin-top:22px">

                <h3>
                    Comments
                </h3>

                ${
                    comments && comments.length
                        ? comments.map(comment => `
                            <div class="comment">

                                <strong>
                                    ${escapeHtml(
                                        comment.supportEngineer
                                    )}
                                </strong>

                                <p style="margin-top:5px">
                                    ${escapeHtml(
                                        comment.comment
                                    )}
                                </p>

                                <small class="muted">
                                    ${escapeHtml(
                                        comment.createdAt
                                    )}
                                </small>

                            </div>
                        `).join("")
                        : `
                            <p
                                class="muted"
                                style="margin-top:10px">
                                No comments yet.
                            </p>
                          `
                }

            </div>

            ${
                role === "SUPPORT_ENGINEER"
                    ? supportActions(ticket)
                    : ""
            }
        `;

        openModal();

    } catch (error) {
        alert(error.message);
    }
}

/* =========================
   SUPPORT ACTIONS
========================= */

function supportActions(ticket) {

    return `

        <div style="margin-top:25px">

            <h3>
                Update Status
            </h3>

            <div
                style="
                    display:flex;
                    gap:10px;
                    margin-top:10px;
                ">

                <select
                    id="status-${ticket.id}">

                    <option
                        value="OPEN"
                        ${
                            ticket.status === "OPEN"
                                ? "selected"
                                : ""
                        }>
                        OPEN
                    </option>

                    <option
                        value="IN_PROGRESS"
                        ${
                            ticket.status === "IN_PROGRESS"
                                ? "selected"
                                : ""
                        }>
                        IN_PROGRESS
                    </option>

                    <option
                        value="RESOLVED"
                        ${
                            ticket.status === "RESOLVED"
                                ? "selected"
                                : ""
                        }>
                        RESOLVED
                    </option>

                    <option
                        value="CLOSED"
                        ${
                            ticket.status === "CLOSED"
                                ? "selected"
                                : ""
                        }>
                        CLOSED
                    </option>

                </select>

                <button
                    class="primary-btn"
                    style="width:auto;margin:0"
                    onclick="updateStatus(${ticket.id})">
                    Update
                </button>

            </div>

            <h3 style="margin-top:22px">
                Add Comment
            </h3>

            <textarea
                id="comment-${ticket.id}"
                placeholder="Write a response...">
            </textarea>

            <button
                class="primary-btn"
                style="width:auto;margin-top:10px"
                onclick="addComment(${ticket.id})">
                Add Comment
            </button>

            <h3 style="margin-top:22px">
                Reassign Ticket
            </h3>

            <div
                style="
                    display:flex;
                    gap:10px;
                    margin-top:10px;
                ">

                <input
                    id="engineer-${ticket.id}"
                    type="number"
                    min="1"
                    placeholder="Support Engineer ID"
                >

                <button
                    class="secondary-btn"
                    onclick="reassignTicket(${ticket.id})">
                    Reassign
                </button>

            </div>

        </div>
    `;
}

/* =========================
   STATUS UPDATE
   Backend uses POST
========================= */

async function updateStatus(ticketId) {

    const status =
        document
            .getElementById(`status-${ticketId}`)
            .value;

    try {

        await apiRequest(
            `/api/support/tickets/${ticketId}/status`,
            {
                method: "POST",

                body: JSON.stringify({
                    status: status
                })
            }
        );

        alert("Ticket status updated.");

        closeModal();

        refreshCurrentView();

    } catch (error) {
        alert(error.message);
    }
}

 

async function addComment(ticketId) {

    const commentElement =
        document.getElementById(
            `comment-${ticketId}`
        );

    const comment =
        commentElement.value.trim();

    if (!comment) {
        alert("Please enter a comment.");
        return;
    }

    try {

        await apiRequest(
            `/api/support/tickets/${ticketId}/comments`,
            {
                method: "POST",

                body: JSON.stringify({
                    comment: comment
                })
            }
        );

        alert("Comment added.");

        closeModal();

        await showTicketDetails(ticketId);

    } catch (error) {
        alert(error.message);
    }
}

 

async function reassignTicket(ticketId) {

    const engineerId =
        Number(
            document
                .getElementById(
                    `engineer-${ticketId}`
                )
                .value
        );

    if (!engineerId) {
        alert("Enter a Support Engineer ID.");
        return;
    }

    try {

        await apiRequest(
            `/api/support/tickets/${ticketId}/reassign`,
            {
                method: "POST",

                body: JSON.stringify({
                    supportEngineerId: engineerId
                })
            }
        );

        alert("Ticket reassigned.");

        closeModal();

        refreshCurrentView();

    } catch (error) {
        alert(error.message);
    }
}

 

async function showHistory(ticketId) {

    try {

        const history =
            await apiRequest(
                `/api/support/tickets/${ticketId}/history`
            );

        modalContent.innerHTML = `

            <h2>
                Ticket History
            </h2>

            <p
                class="muted"
                style="margin-top:5px">
                Ticket #${ticketId}
            </p>

            <div style="margin-top:25px">

                ${
                    history && history.length
                        ? history.map(item => `
                            <div class="history-item">

                                <strong>
                                    ${escapeHtml(
                                        item.action
                                    )}
                                </strong>

                                <p>
                                    ${escapeHtml(
                                        item.details || ""
                                    )}
                                </p>

                                <p>
                                    By:
                                    ${escapeHtml(
                                        item.performedBy
                                    )}
                                </p>

                                <p>
                                    ${escapeHtml(
                                        item.createdAt
                                    )}
                                </p>

                            </div>
                        `).join("")
                        : `
                            <p class="muted">
                                No history found.
                            </p>
                          `
                }

            </div>
        `;

        openModal();

    } catch (error) {
        alert(error.message);
    }
}

 

function refreshCurrentView() {

    const active =
        document.querySelector(
            ".nav-btn.active"
        );

    if (active) {
        loadView(active.dataset.view);
    }
}

 

function openModal() {
    modal.classList.remove("hidden");
}

function closeModal() {
    modal.classList.add("hidden");
}

document
    .getElementById("closeModal")
    .addEventListener(
        "click",
        closeModal
    );

modal.addEventListener(
    "click",
    (event) => {

        if (event.target === modal) {
            closeModal();
        }

    }
);

 

if (token && role) {
    showApp();
}
