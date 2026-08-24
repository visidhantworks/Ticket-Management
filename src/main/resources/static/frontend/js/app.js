const API_BASE =  '';
const state = { token: localStorage.getItem('tm_token'), role: localStorage.getItem('tm_role'), email: localStorage.getItem('tm_email') };
const $ = (id) => document.getElementById(id);

function showToast(message){const t=$('toast');t.textContent=message;t.classList.add('show');setTimeout(()=>t.classList.remove('show'),2800)}
function esc(v){return String(v??'').replace(/[&<>'"]/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[c]))}
function authHeaders(json=true){const h={};if(json)h['Content-Type']='application/json';if(state.token)h.Authorization=`Bearer ${state.token}`;return h}
async function api(path,options={}){const res=await fetch(API_BASE+path,{...options,headers:{...authHeaders(options.body!==undefined),...(options.headers||{})}});let data=null;try{data=await res.json()}catch{try{data=await res.text()}catch{}}
 if(!res.ok){throw new Error(typeof data==='string'?data:(data?.message||data?.error||`Request failed (${res.status})`))}return data}
function setSession(data,email){state.token=data.token;state.role=String(data.role);state.email=email;localStorage.setItem('tm_token',state.token);localStorage.setItem('tm_role',state.role);localStorage.setItem('tm_email',email)}
function logout(callApi=true){const token=state.token;if(callApi&&token)fetch(API_BASE+'/api/auth/logout',{method:'POST',headers:{Authorization:`Bearer ${token}`}}).catch(()=>{});localStorage.removeItem('tm_token');localStorage.removeItem('tm_role');localStorage.removeItem('tm_email');state.token=null;state.role=null;state.email=null;$('appScreen').classList.add('hidden');$('loginScreen').classList.remove('hidden');$('loginForm').reset();}
function navItems(){if(state.role==='REQUESTOR')return [['home','Overview'],['tickets','My Tickets'],['create','Create Ticket']];if(state.role==='SUPPORT_ENGINEER')return [['home','Overview'],['assigned','Assigned Tickets'],['open','Open Tickets']];return [['home','Overview'],['all','All Tickets'],['users','Support Engineers'],['performance','Performance'],['create-user','Create User']];}
function renderNav(active='home'){const n=$('sidebarNav');n.innerHTML=navItems().map(([id,label])=>`<button class="nav-btn ${active===id?'active':''}" data-page="${id}">▸ <span>${label}</span></button>`).join('');n.querySelectorAll('[data-page]').forEach(b=>b.onclick=()=>navigate(b.dataset.page));$('logoutBtn').onclick=()=>logout(true)}
function startApp(){ $('loginScreen').classList.add('hidden');$('appScreen').classList.remove('hidden');$('userEmail').textContent=state.email||'User';$('userRole').textContent=state.role||'ROLE'; $('userInitial').textContent=(state.email||'U')[0].toUpperCase(); navigate('home') }
function navigate(page){renderNav(page);$('pageTitle').textContent=navItems().find(x=>x[0]===page)?.[1]||'Dashboard';const fn=pages[page]||pages.home;fn()}
function statCards(items){return `<div class="stats">${items.map(x=>`<div class="stat"><span>${esc(x[0])}</span><strong>${esc(x[1])}</strong></div>`).join('')}</div>`}
function ticketTable(tickets,actions=true){if(!tickets?.length)return '<div class="empty">No tickets found.</div>';return `<div class="table-wrap"><table class="table"><thead><tr><th>ID</th><th>Title</th><th>Client</th><th>Priority</th><th>Status</th>${actions?'<th>Action</th>':''}</tr></thead><tbody>${tickets.map(t=>`<tr><td>#${esc(t.id)}</td><td><strong>${esc(t.title)}</strong></td><td>${esc(t.bankingClient)}</td><td><span class="badge ${esc(t.priority)}">${esc(t.priority)}</span></td><td><span class="badge ${esc(t.status)}">${esc(t.status)}</span></td>${actions?`<td><button class="small-btn" onclick="viewTicket(${t.id})">View</button></td>`:''}</tr>`).join('')}</tbody></table></div>`}
async function loadTickets(path){$('pageContent').innerHTML='<div class="loading">Loading...</div>';try{const data=await api(path);return Array.isArray(data)?data:[]}catch(e){$('pageContent').innerHTML=`<div class="panel error"><h3>Dashboard could not load</h3><p>${esc(e.message)}</p><p class="muted">Your login was successful. This is an API/dashboard error, not a login error.</p><button class="small-btn" onclick="navigate('home')">Retry</button></div>`;return null}}
const pages={
 home:async()=>{const role=state.role;if(role==='REQUESTOR'){const t=(await loadTickets('/api/tickets/my-tickets'))||[]; if($('pageContent').querySelector('.error')) return; $('pageContent').innerHTML=statCards([['My tickets',t.length],['Open',t.filter(x=>x.status==='OPEN').length],['In progress',t.filter(x=>x.status==='IN_PROGRESS').length],['Resolved',t.filter(x=>x.status==='RESOLVED').length]])+`<div class="panel"><div class="panel-head"><h3>Recent tickets</h3><button class="small-btn primary" onclick="navigate('create')">+ New ticket</button></div>${ticketTable(t.slice(0,8))}</div>`}
 else if(role==='SUPPORT_ENGINEER'){const t=(await loadTickets('/api/support/tickets'))||[]; if($('pageContent').querySelector('.error')) return; const o=(await loadTickets('/api/support/tickets/open'))||[]; if($('pageContent').querySelector('.error')) return; $('pageContent').innerHTML=statCards([['Assigned',t.length],['Open',t.filter(x=>x.status==='OPEN').length],['In progress',t.filter(x=>x.status==='IN_PROGRESS').length],['Open queue',o.length]])+`<div class="panel"><div class="panel-head"><h3>Assigned tickets</h3></div>${ticketTable(t.slice(0,8))}</div>`}
 else {const t=(await loadTickets('/api/admin/tickets'))||[]; if($('pageContent').querySelector('.error')) return; const s=(await loadTickets('/api/admin/users/support'))||[]; if($('pageContent').querySelector('.error')) return; $('pageContent').innerHTML=statCards([['All tickets',t.length],['Open',t.filter(x=>x.status==='OPEN').length],['In progress',t.filter(x=>x.status==='IN_PROGRESS').length],['Support engineers',s.length]])+`<div class="panel"><div class="panel-head"><h3>Latest tickets</h3></div>${ticketTable(t.slice(0,10))}</div>`}},
 tickets:async()=>{const t=(await loadTickets('/api/tickets/my-tickets'))||[];if($('pageContent').querySelector('.error')) return;$('pageContent').innerHTML=`<div class="panel"><div class="panel-head"><h3>My Tickets</h3><button class="small-btn primary" onclick="navigate('create')">+ Create ticket</button></div>${ticketTable(t)}</div>`},
 assigned:async()=>{const t=(await loadTickets('/api/support/tickets'))||[];if($('pageContent').querySelector('.error')) return;$('pageContent').innerHTML=`<div class="panel"><div class="panel-head"><h3>Assigned Tickets</h3></div>${ticketTable(t)}</div>`},
 open:async()=>{const t=(await loadTickets('/api/support/tickets/open'))||[];if($('pageContent').querySelector('.error')) return;$('pageContent').innerHTML=`<div class="panel"><div class="panel-head"><h3>Open Queue</h3></div>${ticketTable(t)}</div>`},
 all:async()=>{const t=(await loadTickets('/api/admin/tickets'))||[];if($('pageContent').querySelector('.error')) return;$('pageContent').innerHTML=`<div class="panel"><div class="panel-head"><h3>All Tickets</h3></div>${ticketTable(t)}</div>`},
 create:()=>{$('pageContent').innerHTML=`<div class="panel"><div class="panel-head"><h3>Create Ticket</h3></div><form id="ticketForm" class="form-grid"><div><label class="form-label">Banking Client ID</label><input class="form-control" name="bankingClientId" type="number" required></div><div><label class="form-label">Title</label><input class="form-control" name="title" required></div><div class="full-span"><label class="form-label">Description</label><textarea class="form-control" name="description" rows="5" required></textarea></div><div><label class="form-label">Category</label><select class="form-control" name="category"><option value="LOGIN">Login</option><option value="PAYMENT">Payment</option><option value="ACCOUNT">Account</option><option value="TECHNICAL">Technical</option><option value="OTHER">Other</option></select></div><div><label class="form-label">Priority</label><select class="form-control" name="priority"><option>LOW</option><option selected>MEDIUM</option><option>HIGH</option></select></div><div><label class="form-label">Attachment</label><input class="form-control" name="attachment" placeholder="Optional URL/path"></div><div class="full-span"><button class="primary-btn" type="submit">Create Ticket</button></div></form></div>`;$('ticketForm').onsubmit=async e=>{e.preventDefault();const f=new FormData(e.target);const body=Object.fromEntries(f.entries());body.bankingClientId=Number(body.bankingClientId);try{await api('/api/tickets',{method:'POST',body:JSON.stringify(body)});showToast('Ticket created');navigate('tickets')}catch(err){showToast(err.message)}}},
 users:async()=>{const users=await loadTickets('/api/admin/users/support');$('pageContent').innerHTML=`<div class="panel"><div class="panel-head"><h3>Support Engineers</h3></div><div class="list">${users.map(u=>`<div class="list-item"><strong>${esc(u.name)}</strong><p>${esc(u.email)}</p></div>`).join('')||'<div class="empty">No support engineers found.</div>'}</div></div>`},
 performance:async()=>{const p=await loadTickets('/api/admin/support/performance');$('pageContent').innerHTML=`<div class="panel"><div class="panel-head"><h3>Support Performance</h3></div><div class="table-wrap"><table class="table"><thead><tr><th>Engineer</th><th>Pending</th><th>Attended</th></tr></thead><tbody>${p.map(x=>`<tr><td><strong>${esc(x.name)}</strong></td><td>${x.pendingTickets}</td><td>${x.attendedTickets}</td></tr>`).join('')||'<tr><td colspan="3">No data</td></tr>'}</tbody></table></div></div>`},
 'create-user':()=>{$('pageContent').innerHTML=`<div class="grid-2"><div class="panel"><h3>Create Requestor</h3><form id="requestorForm"><label class="form-label">Name</label><input class="form-control" name="name" required><label class="form-label">Email</label><input class="form-control" name="email" type="email" required><label class="form-label">Password</label><input class="form-control" name="password" minlength="6" required><button class="primary-btn" style="margin-top:18px">Create Requestor</button></form></div><div class="panel"><h3>Create Support Engineer</h3><form id="supportForm"><label class="form-label">Name</label><input class="form-control" name="name" required><label class="form-label">Email</label><input class="form-control" name="email" type="email" required><label class="form-label">Password</label><input class="form-control" name="password" minlength="6" required><button class="primary-btn" style="margin-top:18px">Create Support Engineer</button></form></div></div>`;bindCreateUser('requestorForm','/api/admin/users/requestor');bindCreateUser('supportForm','/api/admin/users/support')}
};
function bindCreateUser(id,path){$(id).onsubmit=async e=>{e.preventDefault();const body=Object.fromEntries(new FormData(e.target).entries());try{await api(path,{method:'POST',body:JSON.stringify(body)});showToast('User created successfully');e.target.reset()}catch(err){showToast(err.message)}}}
window.viewTicket=async(id)=>{
  const path=state.role==='REQUESTOR'?`/api/tickets/${id}`:state.role==='SUPPORT_ENGINEER'?`/api/support/tickets/${id}`:`/api/admin/tickets/${id}`;
  try{
    const t=await api(path);
    let comments=[];
    let history=[];
    if(state.role==='REQUESTOR'){ 
    comments = await api(`/api/tickets/${id}/comments`);

    try {
        history = await api(`/api/tickets/${id}/history`);
    } catch(e) {
        console.error('History could not be loaded:', e);
        history = [];
    }
}
    else history=await api(state.role==='ADMIN'?`/api/admin/tickets/${id}/history`:`/api/support/tickets/${id}/history`);

    let extra='';
    if(state.role==='SUPPORT_ENGINEER'){
      extra=`<div class="panel"><h3>Update status</h3><div class="action-row" style="margin-top:14px">${['OPEN','IN_PROGRESS','RESOLVED','CLOSED'].map(s=>`<button class="small-btn" onclick="updateStatus(${id},'${s}')">${s}</button>`).join('')}</div></div>
      <div class="panel"><h3>Add comment</h3><form id="commentForm" style="margin-top:14px"><textarea id="commentText" class="form-control" rows="4" placeholder="Write a comment for this ticket..." required></textarea><button class="primary-btn" type="submit" style="margin-top:12px">Add Comment</button></form></div>`;
    }
    if(state.role==='ADMIN'){
      let engineers=await api('/api/admin/users/support');
      extra=`<div class="panel"><h3>Ticket assignment</h3><div class="form-grid" style="margin-top:14px"><div class="full-span"><label class="form-label">Support Engineer</label><select class="form-control" id="engineerSelect"><option value="">Select support engineer</option>${engineers.map(u=>`<option value="${esc(u.id)}">${esc(u.name)} — ${esc(u.email)}</option>`).join('')}</select></div><div><button class="small-btn primary" onclick="assignTicket(${id},false)">Assign</button></div><div><button class="small-btn" onclick="assignTicket(${id},true)">Reassign</button></div></div></div>`;
    }
    document.body.insertAdjacentHTML('beforeend',`<div class="modal" id="ticketModal"><div class="modal-card"><div class="modal-head"><h3>#${esc(t.id)} · ${esc(t.title)}</h3><button class="close" onclick="document.getElementById('ticketModal').remove()">✕</button></div><div class="panel" style="margin-top:18px"><p>${esc(t.description)}</p><p><span class="badge ${esc(t.priority)}">${esc(t.priority)}</span> <span class="badge ${esc(t.status)}">${esc(t.status)}</span></p><p class="muted">Client: ${esc(t.bankingClient)} · Category: ${esc(t.category)}</p></div>${extra}${comments.length?`<div class="panel"><h3>Comments</h3>${comments.map(c=>`<div class="list-item"><strong>${esc(c.supportEngineer)}</strong><p>${esc(c.comment)}</p></div>`).join('')}</div>`:''}${history.length?`<div class="panel"><h3>History</h3>${history.map(h=>`<div class="list-item"><strong>${esc(h.action)} · ${esc(h.performedBy)}</strong><p>${esc(h.details)} · ${esc(h.createdAt)}</p></div>`).join('')}</div>`:''}</div></div>`);
    if(state.role==='SUPPORT_ENGINEER'){
      const form=document.getElementById('commentForm');
      form?.addEventListener('submit', async (e)=>{
        e.preventDefault();
        const text=document.getElementById('commentText')?.value.trim();
        if(!text) return;
        try{
          await api(`/api/support/tickets/${id}/comments`,{method:'POST',body:JSON.stringify({comment:text})});
          showToast('Comment added successfully');
          document.getElementById('ticketModal')?.remove();
          await viewTicket(id);
        }catch(e){showToast(e.message)}
      });
    }
  }catch(e){showToast(e.message)}
};
window.assignTicket=async(id,reassign)=>{
  const engineerId=Number(document.getElementById('engineerSelect')?.value);
  if(!engineerId){showToast('Please select a support engineer');return;}
  const action=reassign?'reassign':'assign';
  try{await api(`/api/admin/tickets/${id}/${action}`,{method:'POST',body:JSON.stringify({supportEngineerId:engineerId})});showToast(reassign?'Ticket reassigned successfully':'Ticket assigned successfully');document.getElementById('ticketModal')?.remove();navigate('all')}catch(e){showToast(e.message)}};
window.updateStatus=async(id,status)=>{try{await api(`/api/support/tickets/${id}/status`,{method:'POST',body:JSON.stringify({status})});showToast('Status updated');document.getElementById('ticketModal')?.remove();navigate('assigned')}catch(e){showToast(e.message)}};
$('togglePassword').onclick=()=>{const p=$('password');p.type=p.type==='password'?'text':'password';$('togglePassword').textContent=p.type==='password'?'Show':'Hide'};
$('loginForm').onsubmit=async e=>{e.preventDefault();$('loginError').textContent='';$('loginBtn').disabled=true;$('loginBtn').textContent='Signing in...';try{const email=$('email').value.trim();const data=await api('/api/auth/login',{method:'POST',body:JSON.stringify({email,password:$('password').value})});setSession(data,email);startApp()}catch(err){$('loginError').textContent=err.message||'Login failed'}finally{$('loginBtn').disabled=false;$('loginBtn').textContent='Sign in'}};
if(state.token&&state.role&&state.email)startApp();
