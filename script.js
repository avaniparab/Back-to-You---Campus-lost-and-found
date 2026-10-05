/**
 * Back to You — Campus Lost & Found System
 * Full Frontend & Backend Integration (PHP 8.5 + MySQL)
 * Vanilla JavaScript
 */

document.addEventListener('DOMContentLoaded', function () {

  // ==========================================================================
  // Helper Functions: Toast, Alerts & Form Validation
  // ==========================================================================
  function showToast(message) {
    let toast = document.querySelector('.toast-notification');
    if (!toast) {
      toast = document.createElement('div');
      toast.className = 'toast-notification';
      document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.classList.add('show');
    setTimeout(function () {
      toast.classList.remove('show');
    }, 3200);
  }

  function showError(inputElement, message) {
    if (!inputElement) return;
    inputElement.classList.add('is-invalid');
    const formGroup = inputElement.closest('.form-group') || inputElement.parentElement;
    if (formGroup) {
      let errDiv = formGroup.querySelector('.form-error-msg');
      if (!errDiv) {
        errDiv = document.createElement('span');
        errDiv.className = 'form-error-msg';
        formGroup.appendChild(errDiv);
      }
      errDiv.textContent = message;
    }
  }

  function clearAllFormErrors(form) {
    if (!form) return;
    const invalidInputs = form.querySelectorAll('.form-control.is-invalid, .is-invalid');
    invalidInputs.forEach(function (input) {
      input.classList.remove('is-invalid');
    });
    const errMsgs = form.querySelectorAll('.form-error-msg');
    errMsgs.forEach(function (msg) {
      msg.remove();
    });
    const existingAlerts = form.querySelectorAll('.form-alert');
    existingAlerts.forEach(function (alert) {
      alert.remove();
    });
  }

  function showFormAlert(form, message, type) {
    if (!form) return;
    const existingAlerts = form.querySelectorAll('.form-alert');
    existingAlerts.forEach(function (alert) {
      alert.remove();
    });
    const alertBox = document.createElement('div');
    alertBox.className = 'form-alert ' + (type || 'info');
    alertBox.innerHTML = message;
    form.insertBefore(alertBox, form.firstChild);
  }

  function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(String(email).toLowerCase());
  }

  function isValidVivaEmail(email) {
    const re = /^[^\s@]+@viva-technology\.org$/i;
    return re.test(String(email).trim());
  }

  function getInitials(name) {
    if (!name) return 'U';
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0].substring(0, 2).toUpperCase();
  }

  // Determine current page path
  const currentPath = window.location.pathname.toLowerCase();
  const adminPages = ['admin-dashboard.html', 'admin-items.html', 'admin-users.html'];
  const studentPages = ['dashboard.html', 'my-reports.html', 'report-lost.html', 'report-found.html'];
  
  const isAdminPage = adminPages.some(page => currentPath.endsWith(page));
  const isStudentPage = studentPages.some(page => currentPath.endsWith(page));

  // ==========================================================================
  // Global Session Check & Authentication Protection
  // ==========================================================================
  let currentUser = null;

  fetch('php/session-check.php')
    .then(res => res.json())
    .then(data => {
      if (data.logged_in && data.user) {
        currentUser = data.user;

        // Security check: non-admin trying to access admin pages
        if (isAdminPage && currentUser.role !== 'ADMIN') {
          window.location.href = 'dashboard.html';
          return;
        }

        // Update user elements across sidebar / topbar
        const nameEls = document.querySelectorAll('.sidebar-user-name, .user-name, #sidebar-user-name');
        nameEls.forEach(el => el.textContent = currentUser.name);

        const avatarEls = document.querySelectorAll('.sidebar-user-avatar, #sidebar-user-avatar');
        avatarEls.forEach(el => el.textContent = getInitials(currentUser.name));

        const roleEls = document.querySelectorAll('.sidebar-user-role, #sidebar-user-role');
        roleEls.forEach(el => el.textContent = (currentUser.role === 'ADMIN' ? 'Administrator' : 'Student'));

        // Update welcome banners if present
        const welcomeHeading = document.getElementById('dashboard-welcome-heading');
        if (welcomeHeading) welcomeHeading.textContent = `Welcome back, ${currentUser.name}!`;

        const userMeta = document.getElementById('dashboard-user-meta');
        if (userMeta) userMeta.innerHTML = `${currentUser.role === 'ADMIN' ? 'Administrator Account' : 'Student Account'} &middot; Campus Mail: <strong>${currentUser.email}</strong>`;

        // Update Public Header navbar login button
        const navLoginLinks = document.querySelectorAll('.nav-links a[href="login.html"]');
        navLoginLinks.forEach(link => {
          if (!link.textContent.toLowerCase().includes('logout')) {
            link.href = (currentUser.role === 'ADMIN' ? 'admin-dashboard.html' : 'dashboard.html');
            link.textContent = 'My Dashboard';
          }
        });
      } else {
        // Not logged in
        if (isAdminPage || isStudentPage) {
          window.location.href = 'login.html';
        }
      }
    })
    .catch(err => {
      if (isAdminPage || isStudentPage) {
        window.location.href = 'login.html';
      }
    });

  // Handle Logout link clicks
  const logoutLinks = document.querySelectorAll('a[href="login.html"]');
  logoutLinks.forEach(link => {
    if (link.textContent.trim().toLowerCase().includes('logout')) {
      link.addEventListener('click', function (e) {
        e.preventDefault();
        fetch('php/logout.php')
          .then(() => { window.location.href = 'login.html'; })
          .catch(() => { window.location.href = 'login.html'; });
      });
    }
  });

  // Set default date for report forms
  const dateLostInput = document.getElementById('lost-date');
  if (dateLostInput && !dateLostInput.value) {
    dateLostInput.value = new Date().toISOString().split('T')[0];
  }
  const dateFoundInput = document.getElementById('found-date');
  if (dateFoundInput && !dateFoundInput.value) {
    dateFoundInput.value = new Date().toISOString().split('T')[0];
  }

  // ==========================================================================
  // SECTION A: REGISTRATION FORM (register.html -> php/register.php)
  // ==========================================================================
  const regNameInput = document.getElementById('reg-name');
  if (regNameInput) {
    const regForm = regNameInput.closest('form');
    if (regForm) {
      regForm.addEventListener('submit', function (e) {
        e.preventDefault();
        clearAllFormErrors(regForm);
        let isValid = true;

        const nameVal = regNameInput.value.trim();
        const emailInput = document.getElementById('reg-email');
        const emailVal = emailInput ? emailInput.value.trim() : '';
        const passInput = document.getElementById('reg-password');
        const passVal = passInput ? passInput.value : '';
        const confirmInput = document.getElementById('reg-confirm-password');
        const confirmVal = confirmInput ? confirmInput.value : '';
        const termsInput = document.getElementById('terms-agree');

        if (!nameVal) {
          showError(regNameInput, 'Please enter your full name.');
          isValid = false;
        }

        if (!emailVal) {
          showError(emailInput, 'Please enter your college email address.');
          isValid = false;
        } else if (!isValidVivaEmail(emailVal)) {
          showError(emailInput, 'Email must end with @viva-technology.org (e.g. name@viva-technology.org).');
          isValid = false;
        }

        if (!passVal) {
          showError(passInput, 'Please enter a password.');
          isValid = false;
        } else if (passVal.length < 8) {
          showError(passInput, 'Password must be at least 8 characters long.');
          isValid = false;
        }

        if (!confirmVal) {
          showError(confirmInput, 'Please confirm your password.');
          isValid = false;
        } else if (confirmVal !== passVal) {
          showError(confirmInput, 'Passwords do not match.');
          isValid = false;
        }

        if (termsInput && !termsInput.checked) {
          showError(termsInput, 'You must agree to the campus honor code.');
          isValid = false;
        }

        if (isValid) {
          fetch('php/register.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              fullname: nameVal,
              email: emailVal,
              password: passVal,
              confirm_password: confirmVal
            })
          })
          .then(res => res.json())
          .then(data => {
            if (data.success) {
              showFormAlert(regForm, '✓ ' + data.message, 'success');
              setTimeout(function () {
                window.location.href = 'login.html';
              }, 1200);
            } else {
              showFormAlert(regForm, '⚠ ' + data.message, 'error');
            }
          })
          .catch(err => {
            showFormAlert(regForm, '⚠ Network error. Unable to reach server. Please check your connection.', 'error');
          });
        }
      });
    }
  }

  // ==========================================================================
  // SECTION B: LOGIN FORM (login.html -> php/login.php)
  // ==========================================================================
  const loginEmailInput = document.getElementById('login-email');
  if (loginEmailInput) {
    const loginForm = loginEmailInput.closest('form');
    if (loginForm) {
      loginForm.addEventListener('submit', function (e) {
        e.preventDefault();
        clearAllFormErrors(loginForm);
        let isValid = true;

        const emailVal = loginEmailInput.value.trim();
        const passInput = document.getElementById('login-password');
        const passVal = passInput ? passInput.value : '';

        if (!emailVal) {
          showError(loginEmailInput, 'Please enter your email address.');
          isValid = false;
        } else if (!isValidEmail(emailVal)) {
          showError(loginEmailInput, 'Please enter a valid email address format.');
          isValid = false;
        }

        if (!passVal) {
          showError(passInput, 'Please enter your password.');
          isValid = false;
        }

        if (isValid) {
          fetch('php/login.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              email: emailVal,
              password: passVal
            })
          })
          .then(res => res.json())
          .then(data => {
            if (data.success) {
              showFormAlert(loginForm, '✓ ' + data.message, 'success');
              const targetPage = (data.user && data.user.role === 'ADMIN') ? 'admin-dashboard.html' : 'dashboard.html';
              setTimeout(function () {
                window.location.href = targetPage;
              }, 1000);
            } else {
              showFormAlert(loginForm, '⚠ ' + data.message, 'error');
            }
          })
          .catch(err => {
            showFormAlert(loginForm, '⚠ Network error. Unable to reach login server.', 'error');
          });
        }
      });
    }
  }

  // ==========================================================================
  // SECTION G: REPORT LOST FORM (report-lost.html -> php/add-item.php)
  // ==========================================================================
  const lostItemInput = document.getElementById('lost-item-name');
  if (lostItemInput) {
    const lostForm = lostItemInput.closest('form');
    if (lostForm) {
      lostForm.addEventListener('submit', function (e) {
        e.preventDefault();
        clearAllFormErrors(lostForm);
        let isValid = true;

        const categorySelect = document.getElementById('lost-category');
        const dateInput = document.getElementById('lost-date');
        const locationInput = document.getElementById('lost-location');
        const descInput = document.getElementById('lost-description');

        if (!lostItemInput.value.trim()) {
          showError(lostItemInput, 'Item name is required.');
          isValid = false;
        }

        if (!categorySelect || !categorySelect.value) {
          showError(categorySelect, 'Please select a category.');
          isValid = false;
        }

        if (!dateInput || !dateInput.value) {
          showError(dateInput, 'Date lost is required.');
          isValid = false;
        }

        if (!locationInput || !locationInput.value.trim()) {
          showError(locationInput, 'Location last seen is required.');
          isValid = false;
        }

        if (!descInput || !descInput.value.trim()) {
          showError(descInput, 'Item description is required.');
          isValid = false;
        }

        if (isValid) {
          fetch('php/add-item.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              item_name: lostItemInput.value.trim(),
              category: categorySelect.value,
              date_lost: dateInput.value,
              location: locationInput.value.trim(),
              description: descInput.value.trim(),
              type: 'LOST'
            })
          })
          .then(res => res.json())
          .then(data => {
            if (data.success) {
              showFormAlert(lostForm, '✓ ' + data.message, 'success');
              setTimeout(function () {
                window.location.href = 'my-reports.html';
              }, 1200);
            } else {
              showFormAlert(lostForm, '⚠ ' + data.message, 'error');
            }
          })
          .catch(err => {
            showFormAlert(lostForm, '⚠ Failed to submit report due to network error.', 'error');
          });
        }
      });
    }
  }

  // ==========================================================================
  // SECTION H: REPORT FOUND FORM (report-found.html -> php/add-item.php)
  // ==========================================================================
  const foundItemInput = document.getElementById('found-item-name');
  if (foundItemInput) {
    const foundForm = foundItemInput.closest('form');
    if (foundForm) {
      foundForm.addEventListener('submit', function (e) {
        e.preventDefault();
        clearAllFormErrors(foundForm);
        let isValid = true;

        const categorySelect = document.getElementById('found-category');
        const dateInput = document.getElementById('found-date');
        const locationInput = document.getElementById('found-location');
        const custodySelect = document.getElementById('found-custody');
        const descInput = document.getElementById('found-description');

        if (!foundItemInput.value.trim()) {
          showError(foundItemInput, 'Item name is required.');
          isValid = false;
        }

        if (!categorySelect || !categorySelect.value) {
          showError(categorySelect, 'Please select a category.');
          isValid = false;
        }

        if (!dateInput || !dateInput.value) {
          showError(dateInput, 'Date found is required.');
          isValid = false;
        }

        if (!locationInput || !locationInput.value.trim()) {
          showError(locationInput, 'Location found is required.');
          isValid = false;
        }

        if (!descInput || !descInput.value.trim()) {
          showError(descInput, 'Item description is required.');
          isValid = false;
        }

        let locStr = locationInput.value.trim();
        if (custodySelect && custodySelect.value) {
          const custodyText = custodySelect.options[custodySelect.selectedIndex].text;
          locStr += ' (Custody: ' + custodyText + ')';
        }

        if (isValid) {
          fetch('php/add-item.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              item_name: foundItemInput.value.trim(),
              category: categorySelect.value,
              date_found: dateInput.value,
              location: locStr,
              description: descInput.value.trim(),
              type: 'FOUND'
            })
          })
          .then(res => res.json())
          .then(data => {
            if (data.success) {
              showFormAlert(foundForm, '✓ ' + data.message, 'success');
              setTimeout(function () {
                window.location.href = 'my-reports.html';
              }, 1200);
            } else {
              showFormAlert(foundForm, '⚠ ' + data.message, 'error');
            }
          })
          .catch(err => {
            showFormAlert(foundForm, '⚠ Failed to submit report due to network error.', 'error');
          });
        }
      });
    }
  }

  // Image preview handler (client side)
  const fileInputs = [document.getElementById('lost-image'), document.getElementById('found-image')];
  fileInputs.forEach(function (fileInput) {
    if (!fileInput) return;
    const dropZone = fileInput.closest('.file-drop-zone');
    if (dropZone) {
      dropZone.addEventListener('click', function (e) {
        if (e.target !== fileInput && e.target.tagName !== 'BUTTON' && !e.target.closest('button')) {
          fileInput.click();
        }
      });
    }
    fileInput.addEventListener('change', function () {
      if (!dropZone) return;
      const file = fileInput.files[0];
      const existingPreview = dropZone.querySelector('.image-preview-wrapper');
      if (existingPreview) existingPreview.remove();

      if (file) {
        const reader = new FileReader();
        reader.onload = function (evt) {
          const previewWrap = document.createElement('div');
          previewWrap.className = 'image-preview-wrapper';
          previewWrap.innerHTML = `
            <img src="${evt.target.result}" alt="Preview" class="image-preview-thumb">
            <div class="image-preview-info">
              <div style="font-weight: 600; font-size: 0.85rem; color: var(--text-dark);">${file.name}</div>
              <div class="text-muted" style="font-size: 0.75rem;">${(file.size / 1024).toFixed(1)} KB (Local Preview)</div>
            </div>
            <button type="button" class="btn btn-secondary btn-sm remove-img-btn">&times; Clear</button>
          `;
          dropZone.appendChild(previewWrap);
          const removeBtn = previewWrap.querySelector('.remove-img-btn');
          removeBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            fileInput.value = '';
            previewWrap.remove();
          });
        };
        reader.readAsDataURL(file);
      }
    });
  });

  // ==========================================================================
  // SECTION E: HOME PAGE DYNAMIC DATA (index.html -> php/get-items.php)
  // ==========================================================================
  const homeItemGrid = document.getElementById('home-item-grid');
  if (homeItemGrid || document.title.includes('Campus Lost & Found')) {
    fetch('php/get-items.php')
      .then(res => res.json())
      .then(data => {
        const items = (data.success && Array.isArray(data.items)) ? data.items : [];
        
        // Update hero metrics
        const reportedStat = document.getElementById('stat-reported-today');
        if (reportedStat) reportedStat.textContent = items.length;

        const resolvedStat = document.getElementById('stat-returned-count');
        const resolvedItems = items.filter(i => i.status === 'RESOLVED');
        if (resolvedStat) resolvedStat.textContent = resolvedItems.length;

        const recentNoticeBox = document.getElementById('stat-recent-box');
        if (recentNoticeBox) {
          if (items.length > 0) {
            const first = items[0];
            recentNoticeBox.innerHTML = `<strong>Recent report:</strong> ${first.type === 'FOUND' ? 'Found' : 'Lost'} "${first.title}" at ${first.location} by ${first.reporter_name}.`;
          } else {
            recentNoticeBox.innerHTML = `<strong>Campus Notice:</strong> Verified lost &amp; found network connecting students with Campus Security.`;
          }
        }

        // Render Recent Active Items (up to 4)
        if (homeItemGrid) {
          homeItemGrid.innerHTML = '';
          const activeItems = items.filter(i => i.status === 'ACTIVE').slice(0, 4);

          if (activeItems.length === 0) {
            homeItemGrid.innerHTML = `
              <div class="empty-state-box" style="grid-column: 1 / -1; text-align: center; padding: 2.5rem 1rem;">
                <svg class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="m21 21-4.35-4.35"/>
                </svg>
                <h3 class="empty-state-title">No Recent Reports</h3>
                <p class="empty-state-desc">There are currently no active lost or found items reported on campus.</p>
                <a href="report-lost.html" class="btn btn-accent btn-sm" style="margin-top: 0.5rem;">Report a Lost Item</a>
              </div>
            `;
          } else {
            activeItems.forEach(item => {
              const card = document.createElement('article');
              card.className = 'item-card';
              const badgeClass = item.type === 'FOUND' ? 'badge-found' : 'badge-lost';
              const badgeText = item.type === 'FOUND' ? 'Found' : 'Lost';

              card.innerHTML = `
                <div class="item-image-wrapper">
                  <svg class="item-placeholder-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                  <div class="item-status-overlay">
                    <span class="badge ${badgeClass}">${badgeText}</span>
                  </div>
                  <div class="item-date-overlay tabular-num">${item.date}</div>
                </div>
                <div class="item-card-body">
                  <div class="item-category-meta">${item.category}</div>
                  <h3 class="item-title">${item.title}</h3>
                  <div class="item-location">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                      <circle cx="12" cy="10" r="3"/>
                    </svg>
                    <span>${item.location}</span>
                  </div>
                  <div class="item-card-footer">
                    <span class="text-muted" style="font-size: 0.8rem;">By ${item.reporter_name}</span>
                    <a href="item-details.html?id=${item.id}" class="btn btn-outline btn-sm">View Details</a>
                  </div>
                </div>
              `;
              homeItemGrid.appendChild(card);
            });
          }
        }
      })
      .catch(err => {
        if (homeItemGrid) {
          homeItemGrid.innerHTML = `
            <div class="empty-state-box" style="grid-column: 1 / -1; text-align: center; padding: 2.5rem 1rem;">
              <h3 class="empty-state-title">No Recent Reports</h3>
              <p class="empty-state-desc">Unable to load campus reports from server.</p>
            </div>
          `;
        }
      });
  }

  // ==========================================================================
  // SECTION D: BROWSE ITEMS (items.html -> php/get-items.php)
  // ==========================================================================
  const searchInput = document.getElementById('search-input');
  const filterCategorySelect = document.getElementById('filter-category');
  const filterLocationSelect = document.getElementById('filter-location');
  const itemGrid = document.querySelector('.item-grid');
  const browseFilterTabs = document.querySelectorAll('.filter-tab-bar .filter-tab');

  if (itemGrid && document.title.includes('Browse')) {
    let allItems = [];
    let activeTypeTab = 'all';

    fetch('php/get-items.php')
      .then(res => res.json())
      .then(data => {
        allItems = (data.success && Array.isArray(data.items)) ? data.items : [];
        updateBrowseTabBadges(allItems);
        applyBrowseFilters();
      })
      .catch(err => {
        renderBrowseItems([]);
      });

    function updateBrowseTabBadges(items) {
      browseFilterTabs.forEach(tab => {
        const type = tab.getAttribute('data-type') || 'all';
        let count = 0;
        if (type === 'all') count = items.length;
        else if (type === 'lost') count = items.filter(i => i.type === 'LOST' && i.status === 'ACTIVE').length;
        else if (type === 'found') count = items.filter(i => i.type === 'FOUND' && i.status === 'ACTIVE').length;
        else if (type === 'resolved') count = items.filter(i => i.status === 'RESOLVED').length;

        const baseLabel = type === 'all' ? 'All Reports' : (type === 'lost' ? 'Lost Items' : (type === 'found' ? 'Found Items' : 'Recently Resolved'));
        tab.textContent = `${baseLabel} (${count})`;
      });
    }

    function renderBrowseItems(itemsToRender) {
      if (!itemGrid) return;
      itemGrid.innerHTML = '';

      if (itemsToRender.length === 0) {
        itemGrid.innerHTML = `
          <div class="empty-state-box" style="grid-column: 1 / -1; text-align: center; padding: 3rem 1rem;">
            <svg class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
            <h3 class="empty-state-title">No items found</h3>
            <p class="empty-state-desc">No reported belongings matched your search or selected filters.</p>
          </div>
        `;
        return;
      }

      itemsToRender.forEach(item => {
        const card = document.createElement('article');
        card.className = 'item-card';
        const isResolved = item.status === 'RESOLVED';
        const badgeClass = isResolved ? 'badge-resolved' : (item.type === 'FOUND' ? 'badge-found' : 'badge-lost');
        const badgeText = isResolved ? 'RESOLVED' : (item.type === 'FOUND' ? 'Found' : 'Lost');

        card.innerHTML = `
          <div class="item-image-wrapper">
            <svg class="item-placeholder-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            <div class="item-status-overlay">
              <span class="badge ${badgeClass}">${badgeText}</span>
            </div>
            <div class="item-date-overlay tabular-num">${item.date}</div>
          </div>
          <div class="item-card-body">
            <div class="item-category-meta">${item.category}</div>
            <h3 class="item-title">${item.title}</h3>
            <div class="item-location">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
              <span>${item.location}</span>
            </div>
            <div class="item-card-footer">
              <span class="text-muted" style="font-size: 0.8rem;">By ${item.reporter_name}</span>
              <a href="item-details.html?id=${item.id}" class="btn btn-outline btn-sm">View Details</a>
            </div>
          </div>
        `;
        itemGrid.appendChild(card);
      });
    }

    function applyBrowseFilters() {
      const q = searchInput ? searchInput.value.toLowerCase().trim() : '';
      const cat = filterCategorySelect ? filterCategorySelect.value.toLowerCase().trim() : '';
      const loc = filterLocationSelect ? filterLocationSelect.value.toLowerCase().trim() : '';

      const filtered = allItems.filter(item => {
        const matchesQuery = !q ||
          (item.title && item.title.toLowerCase().includes(q)) ||
          (item.description && item.description.toLowerCase().includes(q)) ||
          (item.location && item.location.toLowerCase().includes(q)) ||
          (item.category && item.category.toLowerCase().includes(q));

        const matchesCat = !cat || (item.category && item.category.toLowerCase().includes(cat));
        const matchesLoc = !loc || (item.location && item.location.toLowerCase().includes(loc));

        let matchesTab = true;
        if (activeTypeTab === 'lost') matchesTab = (item.type === 'LOST' && item.status === 'ACTIVE');
        else if (activeTypeTab === 'found') matchesTab = (item.type === 'FOUND' && item.status === 'ACTIVE');
        else if (activeTypeTab === 'resolved') matchesTab = (item.status === 'RESOLVED');

        return matchesQuery && matchesCat && matchesLoc && matchesTab;
      });

      renderBrowseItems(filtered);
    }

    if (searchInput) searchInput.addEventListener('input', applyBrowseFilters);
    if (filterCategorySelect) filterCategorySelect.addEventListener('change', applyBrowseFilters);
    if (filterLocationSelect) filterLocationSelect.addEventListener('change', applyBrowseFilters);

    browseFilterTabs.forEach(tab => {
      tab.addEventListener('click', function (e) {
        e.preventDefault();
        browseFilterTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        activeTypeTab = tab.getAttribute('data-type') || 'all';
        applyBrowseFilters();
      });
    });
  }

  // ==========================================================================
  // SECTION F: ITEM DETAILS DYNAMIC FETCH (item-details.html -> php/get-item.php)
  // ==========================================================================
  const detailsContainer = document.getElementById('item-details-container');
  if (detailsContainer || document.title.includes('Item Details')) {
    const itemIdParam = new URLSearchParams(window.location.search).get('id');

    if (!itemIdParam || parseInt(itemIdParam) <= 0) {
      renderItemNotFoundState("No valid item ID was specified in the URL query parameters.");
    } else {
      fetch('php/get-item.php?id=' + parseInt(itemIdParam))
        .then(res => res.json())
        .then(data => {
          if (data.success && data.item) {
            renderItemDetails(data.item);
          } else {
            renderItemNotFoundState(data.message || "Item not found in database.");
          }
        })
        .catch(err => {
          renderItemNotFoundState("Failed to retrieve item details from server.");
        });
    }

    function renderItemNotFoundState(message) {
      if (!detailsContainer) return;
      detailsContainer.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 4rem 1rem;">
          <svg style="width: 64px; height: 64px; color: var(--text-muted); margin-bottom: 1rem;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" x2="12" y1="8" y2="12"/>
            <line x1="12" x2="12.01" y1="16" y2="16"/>
          </svg>
          <h2 style="font-size: 1.75rem; color: var(--text-dark); margin-bottom: 0.5rem;">Item Not Found</h2>
          <p class="text-muted" style="margin-bottom: 1.5rem;">${message}</p>
          <a href="items.html" class="btn btn-primary">&larr; Back to Browse Items</a>
        </div>
      `;
    }

    function renderItemDetails(item) {
      if (!detailsContainer) return;
      document.title = `Item Details: ${item.title} — Back to You`;

      const isResolved = item.status === 'RESOLVED';
      const badgeClass = isResolved ? 'badge-resolved' : (item.type === 'FOUND' ? 'badge-found' : 'badge-lost');
      const badgeText = isResolved ? 'RESOLVED' : (item.type === 'FOUND' ? 'FOUND ITEM' : 'LOST ITEM');

      detailsContainer.innerHTML = `
        <div>
          <div class="details-image-card">
            <div class="details-image-large">
              <svg style="width: 96px; height: 96px; color: var(--primary); opacity: 0.85;" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
              <div style="position: absolute; top: 16px; left: 16px;">
                <span class="badge ${badgeClass}" style="font-size: 0.85rem; padding: 0.4rem 0.85rem;">${badgeText}</span>
              </div>
              <div style="position: absolute; bottom: 16px; left: 16px; right: 16px; background: rgba(22, 38, 44, 0.85); color: #fff; padding: 0.5rem 0.85rem; border-radius: var(--radius-sm); font-size: 0.8rem;">
                Official report record #${item.id} &middot; Submitted by ${item.reporter_name}
              </div>
            </div>
            <div style="padding: 1.25rem; background-color: #FFFFFF; border-top: 1px solid var(--border-light);">
              <h4 style="font-size: 0.95rem; margin-bottom: 0.5rem; color: var(--text-dark);">Claim & Verification Guide</h4>
              <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.5rem;">
                To confirm ownership, be ready to provide:
              </p>
              <ul style="font-size: 0.85rem; color: var(--text-muted); padding-left: 1.2rem; line-height: 1.5;">
                <li>Specific identifying marks or contents</li>
                <li>Valid University Student ID Card</li>
              </ul>
            </div>
          </div>
        </div>

        <div>
          <div class="details-content-card">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem;">
              <span class="badge ${isResolved ? 'badge-resolved' : 'badge-active'}">${isResolved ? 'Resolved' : 'Active Listing'}</span>
              <span class="text-muted tabular-num" style="font-size: 0.85rem;">Report ID: <strong>#ITM-${item.id}</strong></span>
            </div>

            <h1 style="font-size: 2rem; color: var(--text-dark); margin-bottom: 0.5rem;">${item.title}</h1>
            <p style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 1.5rem;">
              Reported on ${item.date}
            </p>

            <div class="details-meta-list">
              <div class="details-meta-item">
                <span class="details-meta-label">Category</span>
                <span class="details-meta-val">${item.category}</span>
              </div>
              <div class="details-meta-item">
                <span class="details-meta-label">Report Type</span>
                <span class="details-meta-val" style="color: ${item.type === 'FOUND' ? 'var(--status-found-text)' : 'var(--status-lost-text)'};">${item.type} Property</span>
              </div>
              <div class="details-meta-item">
                <span class="details-meta-label">Location</span>
                <span class="details-meta-val">${item.location}</span>
              </div>
              <div class="details-meta-item">
                <span class="details-meta-label">Date Discovered</span>
                <span class="details-meta-val tabular-num">${item.date}</span>
              </div>
            </div>

            <div style="margin-bottom: 1.75rem;">
              <h3 style="font-size: 1.05rem; margin-bottom: 0.5rem;">Detailed Description</h3>
              <p style="color: var(--text-body); font-size: 0.95rem; line-height: 1.6;">${item.description}</p>
            </div>

            <div class="reporter-box">
              <div class="reporter-header">
                <div class="reporter-avatar">${getInitials(item.reporter_name)}</div>
                <div>
                  <div class="reporter-name">${item.reporter_name}</div>
                  <div class="reporter-dept">${item.reporter_email} &middot; Registered Campus User</div>
                </div>
              </div>
              <div style="display: flex; gap: 0.75rem; flex-wrap: wrap; margin-top: 1rem;">
                <a href="mailto:${item.reporter_email}" class="btn btn-primary btn-sm">Contact Reporter (${item.reporter_email})</a>
                <a href="items.html" class="btn btn-secondary btn-sm">Browse More Reports</a>
              </div>
            </div>

            <div class="safety-note">
              <strong>Campus Safety Protocol:</strong> Always arrange handovers in public, well-lit campus areas or directly at the Campus Security Control Room.
            </div>
          </div>
        </div>
      `;
    }
  }

  // ==========================================================================
  // SECTION I, J, K: MY REPORTS & RESOLUTION/DELETION (my-reports.html -> php/get-items.php?mine=1)
  // ==========================================================================
  const myReportsTbody = document.getElementById('my-reports-tbody');
  const myReportSubtext = document.getElementById('my-reports-subtext');
  const myReportFilterTabs = document.querySelectorAll('main .filter-tab-bar .filter-tab');

  if (myReportsTbody || document.title.includes('My Reports')) {
    let myItems = [];
    let activeMyTab = 'all';

    function fetchMyReports() {
      fetch('php/get-items.php?mine=1')
        .then(res => res.json())
        .then(data => {
          myItems = (data.success && Array.isArray(data.items)) ? data.items : [];
          updateMyReportBadges();
          applyMyReportsFilter();
        })
        .catch(err => {
          renderMyReportsTable([]);
        });
    }

    function updateMyReportBadges() {
      myReportFilterTabs.forEach(tab => {
        const type = tab.getAttribute('data-type') || 'all';
        let count = 0;
        if (type === 'all') count = myItems.length;
        else if (type === 'lost') count = myItems.filter(i => i.type === 'LOST').length;
        else if (type === 'found') count = myItems.filter(i => i.type === 'FOUND').length;
        else if (type === 'resolved') count = myItems.filter(i => i.status === 'RESOLVED').length;

        const baseLabel = type === 'all' ? 'All Reports' : (type === 'lost' ? 'Lost Items' : (type === 'found' ? 'Found Items' : 'Resolved'));
        tab.textContent = `${baseLabel} (${count})`;
      });
    }

    function applyMyReportsFilter() {
      const filtered = myItems.filter(item => {
        if (activeMyTab === 'lost') return item.type === 'LOST';
        if (activeMyTab === 'found') return item.type === 'FOUND';
        if (activeMyTab === 'resolved') return item.status === 'RESOLVED';
        return true;
      });

      renderMyReportsTable(filtered);
    }

    function renderMyReportsTable(itemsList) {
      if (!myReportsTbody) return;
      myReportsTbody.innerHTML = '';

      if (myReportSubtext) {
        myReportSubtext.textContent = `Showing ${itemsList.length} of ${myItems.length} total submissions`;
      }

      if (itemsList.length === 0) {
        myReportsTbody.innerHTML = `
          <tr>
            <td colspan="6" style="text-align: center; padding: 3rem 1rem;">
              <h3 style="font-size: 1.2rem; color: var(--text-dark); margin-bottom: 0.5rem;">No Reports Submitted Yet</h3>
              <p class="text-muted" style="margin-bottom: 1.25rem;">You haven't submitted any reports matching this filter.</p>
              <a href="report-lost.html" class="btn btn-accent btn-sm" style="margin-right: 0.5rem;">+ Report Lost Item</a>
              <a href="report-found.html" class="btn btn-primary btn-sm">+ Report Found Item</a>
            </td>
          </tr>
        `;
        return;
      }

      itemsList.forEach(item => {
        const tr = document.createElement('tr');
        const typeBadge = item.type === 'FOUND' ? '<span class="badge badge-found">Found</span>' : '<span class="badge badge-lost">Lost</span>';
        const isResolved = item.status === 'RESOLVED';
        const statusBadge = isResolved ? '<span class="badge badge-resolved">RESOLVED</span>' : '<span class="badge badge-active">ACTIVE</span>';

        let actionHtml = '';
        if (isResolved) {
          actionHtml = `
            <div class="table-action-group">
              <a href="item-details.html?id=${item.id}" class="btn btn-secondary btn-sm">View Record</a>
              <button type="button" class="btn btn-danger btn-sm delete-btn" data-id="${item.id}">Delete</button>
            </div>
          `;
        } else {
          actionHtml = `
            <div class="table-action-group">
              <a href="item-details.html?id=${item.id}" class="btn btn-outline btn-sm">View Details</a>
              <button type="button" class="btn btn-secondary btn-sm resolve-btn" data-id="${item.id}">Mark as Resolved</button>
              <button type="button" class="btn btn-danger btn-sm delete-btn" data-id="${item.id}">Delete</button>
            </div>
          `;
        }

        tr.innerHTML = `
          <td>
            <strong>${item.title}</strong>
            <div class="text-muted" style="font-size: 0.8rem;">ID: #ITM-${item.id} &middot; Category: ${item.category}</div>
          </td>
          <td>${typeBadge}</td>
          <td class="tabular-num">${item.date}</td>
          <td>${item.location}</td>
          <td>${statusBadge}</td>
          <td>${actionHtml}</td>
        `;

        myReportsTbody.appendChild(tr);
      });

      // Bind Resolve Buttons
      const resolveButtons = myReportsTbody.querySelectorAll('.resolve-btn');
      resolveButtons.forEach(btn => {
        btn.addEventListener('click', function () {
          const itemId = btn.getAttribute('data-id');
          if (confirm('Are you sure you want to mark this report as RESOLVED?\n\nThis will update MySQL database status.')) {
            fetch('php/update-item.php', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ id: itemId, status: 'RESOLVED' })
            })
            .then(res => res.json())
            .then(data => {
              if (data.success) {
                showToast('✓ Report status updated to RESOLVED in MySQL.');
                fetchMyReports(); // Refresh data from backend
              } else {
                showToast('⚠ ' + (data.message || 'Failed to update item status.'));
              }
            })
            .catch(err => {
              showToast('⚠ Network error updating item status.');
            });
          }
        });
      });

      // Bind Delete Buttons
      const deleteButtons = myReportsTbody.querySelectorAll('.delete-btn');
      deleteButtons.forEach(btn => {
        btn.addEventListener('click', function () {
          const itemId = btn.getAttribute('data-id');
          if (confirm('Are you sure you want to DELETE this report from MySQL?\n\nThis action cannot be undone.')) {
            fetch('php/delete-item.php', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ id: itemId })
            })
            .then(res => res.json())
            .then(data => {
              if (data.success) {
                showToast('✓ Report deleted from MySQL database.');
                fetchMyReports(); // Refresh data from backend
              } else {
                showToast('⚠ ' + (data.message || 'Failed to delete report.'));
              }
            })
            .catch(err => {
              showToast('⚠ Network error deleting report.');
            });
          }
        });
      });
    }

    myReportFilterTabs.forEach(tab => {
      tab.addEventListener('click', function (e) {
        e.preventDefault();
        myReportFilterTabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        activeMyTab = tab.getAttribute('data-type') || 'all';
        applyMyReportsFilter();
      });
    });

    fetchMyReports();
  }

  // ==========================================================================
  // SECTION L: STUDENT DASHBOARD DYNAMIC DATA (dashboard.html -> php/get-items.php?mine=1)
  // ==========================================================================
  const dashTbody = document.getElementById('dashboard-recent-tbody');
  if (dashTbody || document.title.includes('Student Dashboard')) {
    fetch('php/get-items.php?mine=1')
      .then(res => res.json())
      .then(data => {
        const items = (data.success && Array.isArray(data.items)) ? data.items : [];

        // Calculate Dashboard Metrics
        const totalCount = items.length;
        const lostCount = items.filter(i => i.type === 'LOST' && i.status === 'ACTIVE').length;
        const foundCount = items.filter(i => i.type === 'FOUND' && i.status === 'ACTIVE').length;
        const resolvedCount = items.filter(i => i.status === 'RESOLVED').length;

        const lostMetricEl = document.getElementById('metric-lost-count');
        if (lostMetricEl) lostMetricEl.textContent = lostCount;

        const foundMetricEl = document.getElementById('metric-found-count');
        if (foundMetricEl) foundMetricEl.textContent = foundCount;

        const resolvedMetricEl = document.getElementById('metric-resolved-count');
        if (resolvedMetricEl) resolvedMetricEl.textContent = resolvedCount;

        // Render Recent Activity Table
        if (dashTbody) {
          dashTbody.innerHTML = '';
          if (items.length === 0) {
            dashTbody.innerHTML = `
              <tr>
                <td colspan="7" style="text-align: center; padding: 2.5rem 1rem;">
                  <p class="text-muted" style="margin: 0;">No recent activity found. You have not reported any items yet.</p>
                </td>
              </tr>
            `;
          } else {
            items.slice(0, 5).forEach(item => {
              const tr = document.createElement('tr');
              const typeBadge = item.type === 'FOUND' ? '<span class="badge badge-found">Found</span>' : '<span class="badge badge-lost">Lost</span>';
              const isResolved = item.status === 'RESOLVED';
              const statusBadge = isResolved ? '<span class="badge badge-resolved">Resolved</span>' : '<span class="badge badge-active">Active</span>';

              tr.innerHTML = `
                <td>
                  <strong>${item.title}</strong>
                  <div class="text-muted" style="font-size: 0.8rem;">ID: #ITM-${item.id}</div>
                </td>
                <td>${typeBadge}</td>
                <td>${item.category}</td>
                <td>${item.location}</td>
                <td class="tabular-num">${item.date}</td>
                <td>${statusBadge}</td>
                <td>
                  <a href="item-details.html?id=${item.id}" class="btn ${isResolved ? 'btn-secondary' : 'btn-outline'} btn-sm">${isResolved ? 'Archived' : 'View Details'}</a>
                </td>
              `;
              dashTbody.appendChild(tr);
            });
          }
        }
      })
      .catch(err => {
        if (dashTbody) {
          dashTbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2rem;">Unable to load dashboard activity.</td></tr>`;
        }
      });
  }

  // ==========================================================================
  // SECTION N, O, Q: ADMIN DASHBOARD & MANAGE ITEMS (admin-dashboard.html & admin-items.html)
  // ==========================================================================
  const adminRecentTbody = document.getElementById('admin-recent-tbody');
  const adminItemsTbody = document.getElementById('admin-items-tbody');

  if (adminRecentTbody || adminItemsTbody || document.title.includes('Admin')) {
    // Fetch live Admin Stats from php/admin-stats.php
    fetch('php/admin-stats.php')
      .then(res => res.json())
      .then(data => {
        if (data.success && data.stats) {
          const stats = data.stats;
          const usersStat = document.getElementById('admin-stat-users');
          if (usersStat) usersStat.textContent = stats.total_users;

          const lostStat = document.getElementById('admin-stat-lost');
          if (lostStat) lostStat.textContent = stats.total_lost;

          const foundStat = document.getElementById('admin-stat-found');
          if (foundStat) foundStat.textContent = stats.total_found;

          const resolvedStat = document.getElementById('admin-stat-resolved');
          if (resolvedStat) resolvedStat.textContent = stats.total_resolved;
        }
      })
      .catch(err => {
        // Handled silently
      });

    // Fetch items for tables
    fetch('php/get-items.php')
      .then(res => res.json())
      .then(data => {
        const items = (data.success && Array.isArray(data.items)) ? data.items : [];

        // Render Admin Recent Table
        if (adminRecentTbody) {
          adminRecentTbody.innerHTML = '';
          if (items.length === 0) {
            adminRecentTbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 2.5rem;">No campus reports recorded in registry.</td></tr>`;
          } else {
            items.slice(0, 6).forEach(item => {
              const tr = document.createElement('tr');
              const isResolved = item.status === 'RESOLVED';
              tr.innerHTML = `
                <td class="tabular-num">#ITM-${item.id}</td>
                <td><strong>${item.title}</strong></td>
                <td><span class="badge ${item.type === 'FOUND' ? 'badge-found' : 'badge-lost'}">${item.type === 'FOUND' ? 'Found' : 'Lost'}</span></td>
                <td>${item.reporter_name}</td>
                <td>${item.location}</td>
                <td class="tabular-num">${item.date}</td>
                <td><span class="badge ${isResolved ? 'badge-resolved' : 'badge-active'}">${isResolved ? 'Resolved' : 'Active'}</span></td>
                <td>
                  <a href="item-details.html?id=${item.id}" class="btn btn-outline btn-sm">Inspect</a>
                </td>
              `;
              adminRecentTbody.appendChild(tr);
            });
          }
        }

        // Render Admin Items Management Table
        if (adminItemsTbody) {
          renderAdminItemsTable(items);
        }
      })
      .catch(err => {
        if (adminRecentTbody) adminRecentTbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 2rem;">Failed to load items.</td></tr>`;
        if (adminItemsTbody) adminItemsTbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 2rem;">Failed to load items.</td></tr>`;
      });

    function renderAdminItemsTable(items) {
      if (!adminItemsTbody) return;
      adminItemsTbody.innerHTML = '';

      const panelTitle = document.getElementById('admin-items-panel-title');
      if (panelTitle) panelTitle.textContent = `All Campus Reports (${items.length} Total)`;

      const subtext = document.getElementById('admin-items-subtext');
      if (subtext) subtext.textContent = `Displaying ${items.length} records`;

      if (items.length === 0) {
        adminItemsTbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 3rem 1rem;">No items recorded in campus registry.</td></tr>`;
        return;
      }

      items.forEach(item => {
        const tr = document.createElement('tr');
        const isResolved = item.status === 'RESOLVED';
        const typeBadge = item.type === 'FOUND' ? '<span class="badge badge-found">Found</span>' : '<span class="badge badge-lost">Lost</span>';
        const statusBadge = isResolved ? '<span class="badge badge-resolved">Resolved</span>' : '<span class="badge badge-active">Active</span>';

        tr.innerHTML = `
          <td>
            <strong>${item.title}</strong>
            <div class="text-muted tabular-num" style="font-size: 0.8rem;">#ITM-${item.id}</div>
          </td>
          <td>${typeBadge}</td>
          <td>${item.category}</td>
          <td>${item.reporter_name}</td>
          <td>${item.location}</td>
          <td class="tabular-num">${item.date}</td>
          <td>${statusBadge}</td>
          <td>
            <div class="table-action-group">
              <a href="item-details.html?id=${item.id}" class="btn btn-outline btn-sm">View</a>
              ${!isResolved ? `<button type="button" class="btn btn-secondary btn-sm admin-resolve-btn" data-id="${item.id}">Resolve</button>` : ''}
              <button type="button" class="btn btn-danger btn-sm admin-delete-btn" data-id="${item.id}">Delete</button>
            </div>
          </td>
        `;
        adminItemsTbody.appendChild(tr);
      });

      // Bind Admin Resolve Actions
      const resolveBtns = adminItemsTbody.querySelectorAll('.admin-resolve-btn');
      resolveBtns.forEach(btn => {
        btn.addEventListener('click', function () {
          const id = btn.getAttribute('data-id');
          if (confirm('Are you sure you want to mark item #' + id + ' as RESOLVED in MySQL?')) {
            fetch('php/update-item.php', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ id: id, status: 'RESOLVED' })
            })
            .then(res => res.json())
            .then(data => {
              showToast('✓ ' + (data.message || 'Item resolved in MySQL.'));
              location.reload();
            });
          }
        });
      });

      // Bind Admin Delete Actions
      const deleteBtns = adminItemsTbody.querySelectorAll('.admin-delete-btn');
      deleteBtns.forEach(btn => {
        btn.addEventListener('click', function () {
          const id = btn.getAttribute('data-id');
          if (confirm('Are you sure you want to DELETE item #' + id + ' from MySQL?\n\nThis action cannot be undone.')) {
            fetch('php/delete-item.php', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ id: id })
            })
            .then(res => res.json())
            .then(data => {
              showToast('✓ ' + (data.message || 'Item deleted from MySQL.'));
              location.reload();
            });
          }
        });
      });
    }
  }

  // ==========================================================================
  // SECTION P: ADMIN USERS TABLE (admin-users.html -> php/admin-get-users.php)
  // ==========================================================================
  const adminUsersTbody = document.getElementById('admin-users-tbody');
  if (adminUsersTbody) {
    const titleEl = document.getElementById('admin-users-panel-title');
    const subtextEl = document.getElementById('admin-users-subtext');

    fetch('php/admin-get-users.php')
      .then(res => res.json())
      .then(data => {
        if (data.success && Array.isArray(data.users)) {
          const users = data.users;
          if (titleEl) titleEl.textContent = `Registered Users (${users.length} Total)`;
          if (subtextEl) subtextEl.textContent = `Displaying ${users.length} user records`;

          adminUsersTbody.innerHTML = '';
          if (users.length === 0) {
            adminUsersTbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2.5rem;">No registered user accounts found in MySQL.</td></tr>`;
          } else {
            users.forEach(u => {
              const tr = document.createElement('tr');
              tr.innerHTML = `
                <td class="tabular-num"><strong>#USR-${u.id}</strong></td>
                <td>
                  <div style="font-weight: 600;">${u.name}</div>
                  <div class="text-muted" style="font-size: 0.8rem;">Campus User</div>
                </td>
                <td>${u.email}</td>
                <td><span class="badge ${u.role === 'ADMIN' ? 'badge-found' : 'badge-role'}">${u.role}</span></td>
                <td class="tabular-num">${u.created_at || 'Registered'}</td>
                <td><span class="badge badge-active">Active</span></td>
                <td>
                  <div class="table-action-group">
                    <button type="button" class="btn btn-outline btn-sm" disabled>Active</button>
                  </div>
                </td>
              `;
              adminUsersTbody.appendChild(tr);
            });
          }
        } else {
          if (titleEl) titleEl.textContent = `Registered Users (0 Total)`;
          if (subtextEl) subtextEl.textContent = `0 registered users`;
          adminUsersTbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2.5rem;">${data.message || 'Access denied.'}</td></tr>`;
        }
      })
      .catch(err => {
        if (adminUsersTbody) adminUsersTbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2.5rem;">Failed to load user directory from server.</td></tr>`;
      });
  }

});
