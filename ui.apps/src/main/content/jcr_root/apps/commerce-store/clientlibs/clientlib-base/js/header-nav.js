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
            // 1. Check if section is productFullDetail__details
            if (sec.classList.contains("productFullDetail__details")) {
                var contentDl = sec.querySelector("dl");
                var hasDlContent = contentDl && (contentDl.children.length > 0) && (contentDl.textContent || "").trim().length > 0;
                var hasOtherContent = sec.querySelector("table, p, ul, ol");
                if (!hasDlContent && !hasOtherContent) {
                    sec.style.setProperty("display", "none", "important");
                    sec.setAttribute("hidden", "");
                    return;
                }
            }

            // 2. General empty check for any section
            var title = sec.querySelector(".productFullDetail__areaTitle, .productFullDetail__sectionTitle, h1, h2, h3");
            var titleText = title ? (title.textContent || "").trim() : "";
            var fullText = (sec.textContent || "").trim();
            var hasInteractable = sec.querySelector("input, button, select, img, a, table, dl");
            if (!hasInteractable && (!fullText || fullText === titleText)) {
                sec.style.setProperty("display", "none", "important");
                sec.setAttribute("hidden", "");
            }
        });
    }

    function removeTrailingFooterElements() {
        var footers = document.querySelectorAll(".cmp-experiencefragment--footer, footer.experiencefragment");
        footers.forEach(function (footer) {
            var wrapper = footer.closest(".experiencefragment") || footer;
            var sibling = wrapper.nextElementSibling;
            while (sibling) {
                sibling.style.setProperty("display", "none", "important");
                sibling.style.setProperty("height", "0", "important");
                sibling.style.setProperty("min-height", "0", "important");
                sibling.style.setProperty("margin", "0", "important");
                sibling.style.setProperty("padding", "0", "important");
                sibling.setAttribute("hidden", "");
                sibling = sibling.nextElementSibling;
            }
        });

        var rootPars = document.querySelectorAll(".root.container.responsivegrid > .cmp-container > .newpar, .new.newpar.section");
        rootPars.forEach(function (el) {
            el.style.setProperty("display", "none", "important");
            el.style.setProperty("height", "0", "important");
            el.style.setProperty("min-height", "0", "important");
            el.style.setProperty("margin", "0", "important");
            el.style.setProperty("padding", "0", "important");
            el.setAttribute("hidden", "");
        });
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initHeader);
        document.addEventListener("DOMContentLoaded", hideBlankProductSections);
        document.addEventListener("DOMContentLoaded", removeTrailingFooterElements);
    } else {
        initHeader();
        hideBlankProductSections();
        removeTrailingFooterElements();
    }
    window.addEventListener("load", function () {
        hideBlankProductSections();
        removeTrailingFooterElements();
    });
}());

