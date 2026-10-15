const API_BASE = window.KAIROS_API_BASE || window.location.origin;
const API_ROOT = API_BASE.replace(/\/$/, "") + "/api/v1";
const statuses = ["SAVED", "APPLIED", "SCREENING", "ASSESSMENT", "INTERVIEW", "OFFER", "ACCEPTED", "REJECTED", "WITHDRAWN", "REFERRED"];
const sources = ["LINKEDIN", "COMPANY_PORTAL", "REFERRAL", "NAUKRI", "INDEED", "RECRUITER", "CAREER_FAIR", "OTHER"];
const state = { page: 0, data: null, openReferralAppId: null, referrals: {}, messages: {} };
const $ = selector => document.querySelector(selector);
const readable = value => value ? value.replaceAll("_", " ").replace(/\b\w/g, char => char.toUpperCase()) : "Not specified";
const token = () => localStorage.getItem("kairos_token");
const dateToday = () => new Date().toISOString().slice(0, 10);
let toastTimer;
const pendingReferralForms = new WeakSet();
let passwordResetEmail = "";
let passwordResetToken = null;

function options(values, select) { values.forEach(value => select.insertAdjacentHTML("beforeend", "<option value=\"" + value + "\">" + readable(value) + "</option>")); }
function escapeHtml(value = "") { return String(value).replace(/[&<>'"]/g, char => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", "'":"&#39;", '"':"&quot;" })[char]); }

async function request(path, settings = {}) {
  const headers = { "Content-Type": "application/json", ...(settings.headers || {}) };
  if (token()) headers.Authorization = "Bearer " + token();
  const response = await fetch(API_ROOT + path, { ...settings, headers });
  if (response.status === 401 && token() && !path.startsWith("/auth/")) {
    localStorage.removeItem("kairos_token"); showLogin("Your session expired. Please sign in again.");
  }
  if (!response.ok) {
    let detail = "Something went wrong. Please try again.";
    try { detail = (await response.json()).message || detail; } catch (_) { /* empty response */ }
    throw new Error(detail);
  }
  return response.status === 204 ? null : response.json();
}

function showLogin(message = "") {
  $("#app-screen").hidden = true; $("#auth-screen").hidden = false; $("#auth-error").textContent = message;
  $("#auth-forms").hidden = false; $("#forgot-password-button").hidden = false;
  $("#reset-panel").hidden = true; passwordResetEmail = ""; passwordResetToken = null;
}
function clearAuthCredentials() {
  $("#login-form").reset();
  $("#register-form").reset();
  $("#complete-reset-form").reset();
  // Explicitly clear values as browsers may restore them after form.reset().
  document.querySelectorAll("#login-form input, #register-form input, #complete-reset-form input")
    .forEach(input => { input.value = ""; });
}
function showApp(user, successMessage = "") {
  $("#auth-screen").hidden = true; $("#app-screen").hidden = false;
  $("#signed-in-user").textContent = user.name || user.email;
  if (successMessage) showToast(successMessage);
  Promise.all([loadApplications(), loadMetrics(), loadReferralTemplate()]);
}
function showMessage(text = "") { $("#message").textContent = text; }
function showToast(text) {
  const toast = $("#toast");
  toast.textContent = text;
  toast.hidden = false;
  window.clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => { toast.hidden = true; }, 3500);
}
async function loadReferralTemplate() {
  try {
    const result = await request("/referral-template");
    $("#referral-template").value = result.template;
    $("#template-save-status").textContent = "";
  } catch (error) {
    $("#template-save-status").textContent = "Could not load template: " + error.message;
  }
}

function filterQuery() {
  const query = new URLSearchParams({ page: state.page, size: 10, sort: "applicationDate,desc" });
  for (const [key, value] of new FormData($("#filters-form")).entries()) if (value.trim()) query.set(key, value.trim());
  return query.toString();
}

function renderReferralPanel(application) {
  const id = application.id, contacts = state.referrals[id] || [];
  const cards = contacts.map(contact => {
    const message = state.messages[contact.id] || "";
    return `<article class="referral-card" data-referral-card="${contact.id}">
      <div class="referral-fields"><label>Name<input name="name" value="${escapeHtml(contact.name)}" maxlength="150"></label><label>Email<input name="email" type="email" value="${escapeHtml(contact.email || "")}" maxlength="320"></label><label>LinkedIn URL<input name="linkedinUrl" type="url" value="${escapeHtml(contact.linkedinUrl || "")}" maxlength="2048"></label></div>
      <label>Contact message (edit this contact only) <textarea name="messageTemplate" rows="4" placeholder="Use {{name}}, {{job_ID}}, {{role}}, {{company}}, {{jobUrl}}, {{senderName}}">${escapeHtml(contact.messageTemplate || "")}</textarea></label>
      <p class="placeholder-hint">This saved copy is independent of your reusable template.</p>
      <div class="referral-meta"><span class="badge ${contact.status.toLowerCase()}">${readable(contact.status)}</span>${contact.sentAt ? "<span>Sent " + new Date(contact.sentAt).toLocaleString() + "</span>" : ""}${contact.referredAt ? "<span>Referred " + new Date(contact.referredAt).toLocaleString() + "</span>" : ""}</div>
      <div class="referral-actions"><button class="small-button" data-save-referral="${contact.id}" data-app="${id}">Save contact</button><button class="small-button" data-preview="${contact.id}" data-app="${id}">Preview</button><button class="small-button" data-send="${contact.id}" data-app="${id}">${contact.email ? "Send email" : "Generate message"}</button><button class="small-button" data-mark-sent="${contact.id}" data-app="${id}">Mark sent</button><button class="small-button" data-mark-referred="${contact.id}" data-app="${id}">Mark referred</button><button class="small-button delete" data-delete-referral="${contact.id}" data-app="${id}">Delete</button></div>
      ${message ? `<div class="message-box"><pre>${escapeHtml(message)}</pre><button class="small-button" data-copy="${contact.id}" data-app="${id}">Copy</button></div>` : ""}
    </article>`;
  }).join("");
  return `<section class="referral-panel" data-referral-panel="${id}">
    <div class="referral-heading"><div><h3>Referral contacts</h3><p>Add several contacts and tailor each message.</p></div><button class="button quiet" data-send-all="${id}" data-app="${id}">Send to all pending</button></div>
    ${cards || "<p class=\"referral-empty\">No contacts yet.</p>"}
    <form class="add-referral-form" data-add-referral="${id}"><h4>Add a contact</h4><div class="referral-fields"><label>Name<input name="name" required maxlength="150"></label><label>Email<input name="email" type="email" maxlength="320"></label><label>LinkedIn URL<input name="linkedinUrl" type="url" maxlength="2048"></label></div><label>Custom message (optional) <textarea name="messageTemplate" rows="3" placeholder="Leave blank to use your reusable template."></textarea></label><button class="button primary" type="submit">Add contact</button></form>
    <p class="referral-message" aria-live="polite">${escapeHtml(state.messages["application-" + id] || "")}</p>
  </section>`;
}

function renderApplications(data) {
  state.data = data;
  $("#application-list").innerHTML = data.content.map(item => {
    const url = item.jobUrl ? `<a href="${escapeHtml(item.jobUrl)}" target="_blank" rel="noreferrer">Job posting ↗</a>` : "";
    const notes = item.notes ? `<p class="notes">${escapeHtml(item.notes)}</p>` : "";
    const panel = state.openReferralAppId === item.id ? renderReferralPanel(item) : "";
    return `<article class="application-card" data-application="${item.id}">
      <div><h3>${escapeHtml(item.companyName)} <span>— ${escapeHtml(item.jobTitle)}</span></h3><div class="application-meta"><span class="badge ${item.status.toLowerCase()}">${readable(item.status)}</span>${item.jobId ? "<span>Job ID " + escapeHtml(item.jobId) + "</span>" : ""}<span>${escapeHtml(item.location || "Location not set")}</span><span>•</span><span>${readable(item.applicationSource)}</span><span>•</span><time datetime="${item.applicationDate}">${new Date(item.applicationDate + "T00:00:00").toLocaleDateString(undefined, { month:"short", day:"numeric", year:"numeric" })}</time>${url ? "<span>•</span>" + url : ""}</div>${notes}</div>
      <div class="application-actions"><button class="small-button" data-referrals="${item.id}">Referrals</button><button class="small-button" data-edit="${item.id}">Edit</button><button class="small-button delete" data-delete="${item.id}">Delete</button></div>${panel}
    </article>`;
  }).join("");
  $("#empty-state").hidden = data.totalElements !== 0;
  $("#result-count").textContent = data.totalElements + (data.totalElements === 1 ? " entry" : " entries");
  $("#page-status").textContent = data.totalPages ? "Page " + (data.page + 1) + " of " + data.totalPages : "Page 1";
  $("#previous-page").disabled = data.first || data.totalPages === 0; $("#next-page").disabled = data.last || data.totalPages === 0;
}

async function loadApplications() {
  try { showMessage(); renderApplications(await request("/applications?" + filterQuery())); }
  catch (error) { showMessage("Could not load applications: " + error.message); }
}
async function loadMetrics() {
  try {
    const [summary, distribution] = await Promise.all([request("/analytics/summary"), request("/analytics/status-distribution")]);
    const values = { total: summary.totalApplications, active: summary.activeApplications, interview: summary.interviewApplications, offer: summary.offerApplications, accepted: summary.acceptedApplications, rejected: summary.rejectedApplications };
    Object.entries(values).forEach(([key, value]) => $("#metric-" + key).textContent = value);
    const counts = Object.fromEntries(distribution.map(item => [item.status, item.count])), largest = Math.max(1, ...Object.values(counts));
    $("#status-breakdown").innerHTML = statuses.map(status => `<div class="status-row"><span>${readable(status)}</span><strong>${counts[status] || 0}</strong><div class="bar"><span style="width:${((counts[status] || 0) / largest) * 100}%"></span></div></div>`).join("");
  } catch (_) { $("#status-breakdown").textContent = "Metrics are unavailable right now."; }
}

function openForm(item = null) {
  $("#application-form").reset(); $("#form-error").textContent = ""; $("#application-id").value = item?.id || "";
  $("#form-kicker").textContent = item ? "UPDATE APPLICATION" : "NEW APPLICATION"; $("#form-title").textContent = item ? "Edit application" : "Add an application";
  $("#save-button").textContent = item ? "Update application" : "Save application";
  ["jobId", "companyName", "jobTitle", "jobUrl", "location", "applicationSource", "status", "applicationDate", "notes"].forEach(key => { if (item?.[key] != null) $("#" + key).value = item[key]; });
  if (!item) $("#applicationDate").value = dateToday(); $("#application-dialog").showModal();
}

async function saveApplication(event) {
  event.preventDefault(); if (!$("#application-form").reportValidity()) return;
  const id = $("#application-id").value, keys = ["jobId", "companyName", "jobTitle", "jobUrl", "location", "applicationSource", "status", "applicationDate", "notes"];
  const payload = Object.fromEntries(keys.map(key => [key, $("#" + key).value.trim() || null]));
  try {
    $("#save-button").disabled = true;
    await request("/applications" + (id ? "/" + id : ""), { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
    $("#application-dialog").close(); state.page = 0; await Promise.all([loadApplications(), loadMetrics()]);
  } catch (error) { $("#form-error").textContent = error.message; } finally { $("#save-button").disabled = false; }
}
async function deleteApplication(id) {
  const item = state.data?.content.find(application => application.id === id);
  if (!confirm("Delete " + (item?.companyName || "this application") + "? This cannot be undone.")) return;
  try { await request("/applications/" + id, { method: "DELETE" }); await Promise.all([loadApplications(), loadMetrics()]); }
  catch (error) { showMessage("Could not delete application: " + error.message); }
}
async function toggleReferrals(id) {
  if (state.openReferralAppId === id) { state.openReferralAppId = null; renderApplications(state.data); return; }
  state.openReferralAppId = id; state.referrals[id] = null; renderApplications(state.data);
  try { state.referrals[id] = await request("/applications/" + id + "/referrals"); renderApplications(state.data); }
  catch (error) { state.referrals[id] = []; state.messages["application-" + id] = error.message; renderApplications(state.data); }
}
function referralForm(appId, id) {
  const panel = document.querySelector('[data-referral-panel="' + appId + '"]');
  const form = id ? panel.querySelector('[data-referral-card="' + id + '"]') : panel.querySelector('[data-add-referral="' + appId + '"]');
  if (id) return Object.fromEntries(["name", "email", "linkedinUrl", "messageTemplate"].map(key => {
    const value = form.querySelector('[name="' + key + '"]').value.trim();
    return [key, value || null];
  }));
  const values = new FormData(form);
  return Object.fromEntries(["name", "email", "linkedinUrl", "messageTemplate"].map(key => [key, values.get(key)?.trim() || null]));
}
async function refreshReferrals(appId) { state.referrals[appId] = await request("/applications/" + appId + "/referrals"); renderApplications(state.data); }
async function saveReferral(appId, id, payload = null) {
  await request("/applications/" + appId + "/referrals" + (id ? "/" + id : ""), {
    method: id ? "PUT" : "POST", body: JSON.stringify(payload || referralForm(appId, id))
  });
  await refreshReferrals(appId);
}
async function authenticate(event, path) {
  event.preventDefault();
  try {
    const result = await request("/auth/" + path, { method: "POST", body: JSON.stringify(Object.fromEntries(new FormData(event.currentTarget).entries())) });
    localStorage.setItem("kairos_token", result.token);
    clearAuthCredentials();
    showApp(result.user, path === "register" ? "Account created successfully." : "Signed in successfully.");
  } catch (error) { $("#auth-error").textContent = error.message; }
}

options(statuses, $("#status")); options(statuses, $("#status-filter")); options(sources, $("#applicationSource")); options(sources, $("#source-filter"));
$("#login-form").addEventListener("submit", event => authenticate(event, "login"));
$("#register-form").addEventListener("submit", event => authenticate(event, "register"));
$("#forgot-password-button").addEventListener("click", () => {
  passwordResetEmail = $("#login-form").elements.namedItem("email").value.trim();
  $("#reset-email").value = passwordResetEmail;
  $("#auth-error").textContent = "";
  $("#auth-forms").hidden = true;
  $("#forgot-password-button").hidden = true; $("#reset-panel").hidden = false;
  $("#request-reset-form").hidden = false; $("#verify-reset-form").hidden = true; $("#complete-reset-form").hidden = true;
  $("#reset-status").textContent = "Enter your email to receive a verification code.";
});
$("#request-reset-form").addEventListener("submit", async event => {
  event.preventDefault();
  passwordResetEmail = $("#reset-email").value.trim();
  const status = $("#reset-status"); status.textContent = "Sending code…";
  try {
    const result = await request("/auth/password-reset/request", {
      method: "POST", body: JSON.stringify({ email: passwordResetEmail })
    });
    status.textContent = result.message + " Check your inbox and spam folder.";
    $("#verify-reset-form").hidden = false;
  } catch (error) { status.textContent = error.message; }
});
$("#verify-reset-form").addEventListener("submit", async event => {
  event.preventDefault();
  const status = $("#reset-status"); status.textContent = "Verifying code…";
  try {
    const result = await request("/auth/password-reset/verify", {
      method: "POST", body: JSON.stringify({ email: passwordResetEmail, code: $("#reset-code").value.trim() })
    });
    passwordResetToken = result.resetToken;
    $("#request-reset-form").hidden = true; $("#verify-reset-form").hidden = true; $("#complete-reset-form").hidden = false;
    status.textContent = "Code verified. Choose a new password.";
  } catch (error) { status.textContent = error.message; }
});
$("#complete-reset-form").addEventListener("submit", async event => {
  event.preventDefault();
  const values = Object.fromEntries(new FormData(event.currentTarget).entries());
  const status = $("#reset-status");
  if (values.newPassword !== values.confirmPassword) { status.textContent = "Passwords do not match."; return; }
  status.textContent = "Updating password…";
  try {
    const result = await request("/auth/password-reset/complete", {
      method: "POST", body: JSON.stringify({ resetToken: passwordResetToken, ...values })
    });
    localStorage.setItem("kairos_token", result.token);
    clearAuthCredentials();
    showApp(result.user, "Password updated. You are signed in.");
  } catch (error) { status.textContent = error.message; }
});
$("#back-to-signin").addEventListener("click", () => showLogin());
$("#referral-template-form").addEventListener("submit", async event => {
  event.preventDefault();
  const status = $("#template-save-status");
  status.textContent = "Saving…";
  try {
    const result = await request("/referral-template", {
      method: "PUT", body: JSON.stringify({ template: $("#referral-template").value })
    });
    $("#referral-template").value = result.template;
    status.textContent = "Saved. New referral contacts will use this template.";
    showToast("Referral message template saved.");
  } catch (error) { status.textContent = "Could not save template: " + error.message; }
});
$("#sign-out-button").addEventListener("click", () => { localStorage.removeItem("kairos_token"); clearAuthCredentials(); showLogin(); });
$("#new-entry-button").addEventListener("click", () => openForm());
$("#close-dialog").addEventListener("click", () => $("#application-dialog").close()); $("#cancel-dialog").addEventListener("click", () => $("#application-dialog").close());
$("#application-form").addEventListener("submit", saveApplication);
$("#filters-form").addEventListener("submit", event => { event.preventDefault(); state.page = 0; loadApplications(); });
$("#clear-filters").addEventListener("click", () => { $("#filters-form").reset(); state.page = 0; loadApplications(); });
$("#previous-page").addEventListener("click", () => { if (state.page > 0) { state.page--; loadApplications(); } });
$("#next-page").addEventListener("click", () => { if (state.data && !state.data.last) { state.page++; loadApplications(); } });

$("#application-list").addEventListener("submit", async event => {
  const form = event.target.closest("[data-add-referral]"); if (!form) return; event.preventDefault();
  if (pendingReferralForms.has(form)) return;
  pendingReferralForms.add(form);
  const submitButton = form.querySelector('button[type="submit"]');
  submitButton.disabled = true;
  submitButton.textContent = "Adding…";
  try { await saveReferral(form.dataset.addReferral, null, Object.fromEntries(new FormData(form).entries())); }
  catch (error) { state.messages["application-" + form.dataset.addReferral] = error.message; renderApplications(state.data); }
  finally {
    pendingReferralForms.delete(form);
    submitButton.disabled = false;
    submitButton.textContent = "Add contact";
  }
});
$("#application-list").addEventListener("click", async event => {
  const button = event.target.closest("button"); if (!button) return;
  const appId = button.dataset.app;
  const id = button.dataset.saveReferral || button.dataset.preview || button.dataset.send || button.dataset.markSent || button.dataset.markReferred || button.dataset.deleteReferral || button.dataset.copy;
  try {
    if (button.dataset.referrals) return toggleReferrals(button.dataset.referrals);
    if (button.dataset.edit) return openForm(state.data.content.find(item => item.id === button.dataset.edit));
    if (button.dataset.delete) return deleteApplication(button.dataset.delete);
    if (button.dataset.saveReferral) return saveReferral(appId, id);
    if (button.dataset.preview) { const p = await request("/applications/" + appId + "/referrals/" + id + "/message"); state.messages[id] = p.message; renderApplications(state.data); return; }
    if (button.dataset.send) {
      await saveReferral(appId, id);
      const result = await request("/applications/" + appId + "/referrals/" + id + "/send", { method: "POST", body: "{}" });
      state.messages[id] = result.message || result.error || readable(result.outcome); await refreshReferrals(appId); return;
    }
    if (button.dataset.markSent || button.dataset.markReferred) {
      await request("/applications/" + appId + "/referrals/" + id + "/" + (button.dataset.markSent ? "mark-sent" : "mark-referred"), { method: "POST" });
      await refreshReferrals(appId); return;
    }
    if (button.dataset.deleteReferral) { await request("/applications/" + appId + "/referrals/" + id, { method: "DELETE" }); await refreshReferrals(appId); return; }
    if (button.dataset.copy) { await navigator.clipboard.writeText(state.messages[id] || ""); state.messages["application-" + appId] = "Message copied."; renderApplications(state.data); return; }
    if (button.dataset.sendAll) {
      const results = await request("/applications/" + appId + "/referrals/send-all", { method: "POST" });
      state.messages["application-" + appId] = results.map(r => readable(r.outcome) + (r.error ? ": " + r.error : "")).join(" · ") || "No pending contacts.";
      await refreshReferrals(appId);
    }
  } catch (error) { state.messages["application-" + appId] = error.message; renderApplications(state.data); }
});

async function start() {
  if (!token()) return showLogin();
  try { showApp(await request("/auth/me")); }
  catch (_) { localStorage.removeItem("kairos_token"); showLogin("Your session expired. Please sign in again."); }
}
start();
