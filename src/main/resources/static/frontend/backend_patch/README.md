Admin dashboard backend patch

Replace your existing ticket_management/controller/AdminController.java with the included file.

It adds:
GET  /api/admin/tickets/{ticketId}
GET  /api/admin/tickets/{ticketId}/history

The existing assign/reassign endpoints were already present and are now wired into the admin frontend.
