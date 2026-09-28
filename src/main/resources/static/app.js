const API = "/api/v1";
const statuses = ["SAVED", "APPLIED", "SCREENING", "ASSESSMENT", "INTERVIEW", "OFFER", "ACCEPTED", "REJECTED", "WITHDRAWN"];
const sources = ["LINKEDIN", "COMPANY_PORTAL", "REFERRAL", "NAUKRI", "INDEED", "RECRUITER", "CAREER_FAIR", "OTHER"];
const state = { page: 0, data: null };

const $ = (selector) => document.querySelector(selector);
const readable = (value) => value ? value.replaceAll("_", " ").replace(/\b\w/g, char => char.toUpperCase()) : "Not specified";
const dateToday = () => new Date().toISOString().slice(0, 10);

function options(values, select) {
  values.forEach(value => select.insertAdjacentHTML("beforeend", `<option value="${value}">${readable(value)}</option>`));
}

function escapeHtml(value = "") {
  return String(value).replace(/[&<>'"]/g, char => ({ "&":"&amp;", "<":"&lt;", ">":"&gt;", "'":"&#39;", '"':"&quot;" })[char]);
}

async function request(path, settings = {}) {
  const response = await fetch(`${API}${path}`, { headers: { "Content-Type": "application/json" }, ...settings });
  if (!response.ok) {
    let detail = "Something went wrong. Please try again.";
    try { detail = (await response.json()).message || detail; } catch (_) { /* empty response */ }
    throw new Error(detail);
  }
  return response.status === 204 ? null : response.json();
}

function showMessage(text = "") { $("#message").textContent = text; }

function filterQuery() {
  const form = new FormData($("#filters-form"));
  const query = new URLSearchParams({ page: state.page, size: 10, sort: "applicationDate,desc" });
  for (const [key, value] of form.entries()) if (value.trim()) query.set(key, value.trim());
  return query.toString();
}

function renderApplications(data) {
  state.data = data;
  const list = $("#application-list");
  list.innerHTML = data.content.map(item => {
    const url = item.jobUrl ? `<a href="${escapeHtml(item.jobUrl)}" target="_blank" rel="noreferrer">Job posting ↗</a>` : "";
    const notes = item.notes ? `<p class="notes">${escapeHtml(item.notes)}</p>` : "";
    return `<article class="application-card">
      <div><h3>${escapeHtml(item.companyName)} <span>— ${escapeHtml(item.jobTitle)}</span></h3>
      <div class="application-meta"><span class="badge ${item.status.toLowerCase()}">${readable(item.status)}</span><span>${escapeHtml(item.location || "Location not set")}</span><span>•</span><span>${readable(item.applicationSource)}</span><span>•</span><time datetime="${item.applicationDate}">${new Date(`${item.applicationDate}T00:00:00`).toLocaleDateString(undefined, { month:"short", day:"numeric", year:"numeric" })}</time>${url ? `<span>•</span>${url}` : ""}</div>${notes}</div>
      <div class="application-actions"><button class="small-button" type="button" data-edit="${item.id}">Edit</button><button class="small-button delete" type="button" data-delete="${item.id}">Delete</button></div>
    </article>`;
  }).join("");
  $("#empty-state").hidden = data.totalElements !== 0;
  $("#result-count").textContent = `${data.totalElements} ${data.totalElements === 1 ? "entry" : "entries"}`;
  $("#page-status").textContent = data.totalPages ? `Page ${data.page + 1} of ${data.totalPages}` : "Page 1";
  $("#previous-page").disabled = data.first || data.totalPages === 0;
  $("#next-page").disabled = data.last || data.totalPages === 0;
}

async function loadApplications() {
  try { showMessage(); renderApplications(await request(`/applications?${filterQuery()}`)); }
  catch (error) { showMessage(`Could not load applications: ${error.message}`); }
}

async function loadMetrics() {
  try {
    const [summary, distribution] = await Promise.all([request("/analytics/summary"), request("/analytics/status-distribution")]);
    const metricValues = { total: summary.totalApplications, active: summary.activeApplications, interview: summary.interviewApplications, offer: summary.offerApplications, accepted: summary.acceptedApplications, rejected: summary.rejectedApplications };
    Object.entries(metricValues).forEach(([key, value]) => $(`#metric-${key}`).textContent = value);
    const countByStatus = Object.fromEntries(distribution.map(item => [item.status, item.count]));
    const largest = Math.max(1, ...Object.values(countByStatus));
    $("#status-breakdown").innerHTML = statuses.map(status => `<div class="status-row"><span>${readable(status)}</span><strong>${countByStatus[status] || 0}</strong><div class="bar"><span style="width:${((countByStatus[status] || 0) / largest) * 100}%"></span></div></div>`).join("");
  } catch (error) { $("#status-breakdown").textContent = "Metrics are unavailable right now."; }
}

function openForm(item = null) {
  const form = $("#application-form");
  form.reset(); $("#form-error").textContent = "";
  $("#application-id").value = item?.id || "";
  $("#form-kicker").textContent = item ? "UPDATE APPLICATION" : "NEW APPLICATION";
  $("#form-title").textContent = item ? "Edit application" : "Add an application";
  $("#save-button").textContent = item ? "Update application" : "Save application";
  ["companyName", "jobTitle", "jobUrl", "location", "applicationSource", "status", "applicationDate", "notes"].forEach(key => { if (item?.[key] != null) $(`#${key}`).value = item[key]; });
  if (!item) $("#applicationDate").value = dateToday();
  $("#application-dialog").showModal();
}

async function saveApplication(event) {
  event.preventDefault();
  const form = $("#application-form");
  if (!form.reportValidity()) return;
  const id = $("#application-id").value;
  const payload = Object.fromEntries(["companyName", "jobTitle", "jobUrl", "location", "applicationSource", "status", "applicationDate", "notes"].map(key => [key, $(`#${key}`).value.trim() || null]));
  // Optional fields are nullable in the API; only status/date/company/title must be present.
  try {
    $("#save-button").disabled = true;
    await request(`/applications${id ? `/${id}` : ""}`, { method: id ? "PUT" : "POST", body: JSON.stringify(payload) });
    $("#application-dialog").close(); state.page = 0; await Promise.all([loadApplications(), loadMetrics()]);
  } catch (error) { $("#form-error").textContent = error.message; }
  finally { $("#save-button").disabled = false; }
}

async function deleteApplication(id) {
  const item = state.data?.content.find(application => application.id === id);
  if (!confirm(`Delete ${item?.companyName || "this application"}? This cannot be undone.`)) return;
  try { await request(`/applications/${id}`, { method: "DELETE" }); await Promise.all([loadApplications(), loadMetrics()]); }
  catch (error) { showMessage(`Could not delete application: ${error.message}`); }
}

options(statuses, $("#status")); options(statuses, $("#status-filter")); options(sources, $("#applicationSource")); options(sources, $("#source-filter"));
$("#new-entry-button").addEventListener("click", () => openForm());
$("#close-dialog").addEventListener("click", () => $("#application-dialog").close());
$("#cancel-dialog").addEventListener("click", () => $("#application-dialog").close());
$("#application-form").addEventListener("submit", saveApplication);
$("#filters-form").addEventListener("submit", event => { event.preventDefault(); state.page = 0; loadApplications(); });
$("#clear-filters").addEventListener("click", () => { $("#filters-form").reset(); state.page = 0; loadApplications(); });
$("#previous-page").addEventListener("click", () => { if (state.page > 0) { state.page--; loadApplications(); } });
$("#next-page").addEventListener("click", () => { if (state.data && !state.data.last) { state.page++; loadApplications(); } });
$("#application-list").addEventListener("click", event => { const editId = event.target.dataset.edit; const deleteId = event.target.dataset.delete; if (editId) openForm(state.data.content.find(item => item.id === editId)); if (deleteId) deleteApplication(deleteId); });
loadApplications(); loadMetrics();
