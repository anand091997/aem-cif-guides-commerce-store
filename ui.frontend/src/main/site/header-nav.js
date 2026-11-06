(function () {
    "use strict";

    function initHeader() {
        var header = document.querySelector("header.experiencefragment");
        if (!header) return;

        // 1. Hide utility policy pages (About Us, Privacy Policy, Terms) from desktop navigation
        var staticHrefs = ["about-us", "privacy-policy", "terms-conditions"];
        var navItems = header.querySelectorAll(".cmp-navigation > .cmp-navigation__group > .cmp-navigation__item");
        navItems.forEach(function (item) {
            var link = item.querySelector("a");
            if (link) {
                var href = link.getAttribute("href") || "";
                staticHrefs.forEach(function (slug) {
                    if (href.indexOf(slug) !== -1) {
                        item.classList.add("plx-nav-utility-item");
                    }
                });
            }
        });

        // 2. Ensure mobile hamburger button exists
        var existingToggle = header.querySelector(".plx-nav-toggle");
        if (!existingToggle) {
            var toggleBtn = document.createElement("button");
            toggleBtn.className = "plx-nav-toggle";
            toggleBtn.type = "button";
            toggleBtn.setAttribute("aria-label", "Toggle navigation");
            toggleBtn.innerHTML = '<span class="plx-nav-toggle-icon"><span></span><span></span><span></span></span>';

            var container = header.querySelector(".cmp-container > .aem-Grid") || header.querySelector(".cmp-container");
            if (container) {
                container.insertBefore(toggleBtn, container.firstChild);
            }

            toggleBtn.addEventListener("click", function (e) {
                e.preventDefault();
                header.classList.toggle("is-open");
                document.body.classList.toggle("plx-nav-drawer-open");
            });
        }

        // 3. Add Close button inside .navigation drawer if not present
        var navigation = header.querySelector(".navigation");
        if (navigation && !navigation.querySelector(".plx-nav-drawer-close")) {
            var closeBtn = document.createElement("button");
            closeBtn.className = "plx-nav-drawer-close";
            closeBtn.type = "button";
            closeBtn.setAttribute("aria-label", "Close navigation");
            closeBtn.innerHTML = "&times;";
            closeBtn.addEventListener("click", function (e) {
                e.preventDefault();
                header.classList.remove("is-open");
                document.body.classList.remove("plx-nav-drawer-open");
            });
            navigation.insertBefore(closeBtn, navigation.firstChild);
        }

        // 4. In mobile drawer, allow submenus to toggle on tap
        var categoryItems = header.querySelectorAll(".navigation .cmp-navigation__item");
        categoryItems.forEach(function (item) {
            var subGroup = item.querySelector(".cmp-navigation__group");
            if (subGroup) {
                item.classList.add("has-submenu");
                var link = item.querySelector(":scope > .cmp-navigation__item-link");
                if (link && !item.querySelector(".plx-submenu-toggle")) {
                    var subToggle = document.createElement("button");
                    subToggle.className = "plx-submenu-toggle";
                    subToggle.type = "button";
                    subToggle.setAttribute("aria-label", "Toggle submenu");
                    subToggle.innerHTML = "<span></span>";
                    item.insertBefore(subToggle, subGroup);

                    subToggle.addEventListener("click", function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        item.classList.toggle("submenu-open");
                    });
                }
            }
        });

        // 5. Close drawer on navigation click
        if (navigation) {
            navigation.querySelectorAll("a").forEach(function (link) {
                link.addEventListener("click", function () {
                    header.classList.remove("is-open");
                    document.body.classList.remove("plx-nav-drawer-open");
                });
            });
        }
    }

    function hideBlankProductSections() {
        var sections = document.querySelectorAll(".productFullDetail__details, .productFullDetail__section");
        sections.forEach(function (sec) {
            var hasContentElement = sec.querySelector("dl, h2, h3, h4, p, div, table, ul, ol, span, form, input, button");
            var text = (sec.textContent || "").trim();
            if (!hasContentElement && !text) {
                sec.style.setProperty("display", "none", "important");
                sec.setAttribute("hidden", "");
            }
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initHeader);
        document.addEventListener("DOMContentLoaded", hideBlankProductSections);
    } else {
        initHeader();
        hideBlankProductSections();
    }
}());

