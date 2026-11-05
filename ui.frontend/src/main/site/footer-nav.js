(function () {
    "use strict";

    var query = window.matchMedia("(max-width: 767px)");

    function columns() {
        return document.querySelectorAll(".plx-footer-nav .plx-footer-col");
    }

    function sync() {
        columns().forEach(function (column) {
            var heading = column.querySelector("h4");
            var list = column.querySelector("ul");
            if (!heading || !list) {
                return;
            }
            if (!query.matches) {
                column.classList.remove("is-open");
                heading.removeAttribute("aria-expanded");
                heading.removeAttribute("role");
                heading.removeAttribute("tabindex");
                return;
            }
            var open = column.classList.contains("is-open");
            heading.setAttribute("role", "button");
            heading.setAttribute("tabindex", "0");
            heading.setAttribute("aria-expanded", open ? "true" : "false");
        });
    }

    function toggle(event) {
        if (!query.matches) {
            return;
        }
        var heading = event.target.closest(".plx-footer-nav .plx-footer-col h4");
        if (!heading) {
            return;
        }
        event.preventDefault();
        heading.closest(".plx-footer-col").classList.toggle("is-open");
        sync();
    }

    document.addEventListener("click", toggle);
    document.addEventListener("keydown", function (event) {
        if (event.key !== "Enter" && event.key !== " ") {
            return;
        }
        toggle(event);
    });

    if (query.addEventListener) {
        query.addEventListener("change", sync);
    } else if (query.addListener) {
        query.addListener(sync);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", sync);
    } else {
        sync();
    }
}());

