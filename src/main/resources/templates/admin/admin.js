"use strict";

/* ==========================
   Global Variables
========================== */

const API = "/admin";

let rooms = [];
let filteredRooms = [];
let selectedImages = [];
let deleteRoomId = null;


/* ==========================
   Window Load
========================== */

window.onload = function () {

    bindSidebar();

    navigateTo("dashboard");

    loadDashboard();

};


/* ==========================
   Sidebar Navigation
========================== */

function bindSidebar() {

    document.querySelectorAll(".nav-item[data-page]").forEach(item => {

        item.onclick = function (e) {

            e.preventDefault();

            navigateTo(this.dataset.page);

        };

    });

}


/* ==========================
   Page Navigation
========================== */

function navigateTo(page) {

    document.querySelectorAll(".page").forEach(p => {

        p.classList.remove("active");

    });

    const current = document.getElementById("page-" + page);

    if (current) {

        current.classList.add("active");

    }

    document.querySelectorAll(".nav-item").forEach(n => {

        n.classList.remove("active");

    });

    const nav = document.querySelector(".nav-item[data-page='" + page + "']");

    if (nav) {

        nav.classList.add("active");

    }

    const breadcrumb = document.getElementById("breadcrumbPage");

    if (breadcrumb) {

        breadcrumb.innerText = page.replace("-", " ");

    }

    if (page === "dashboard") {

        loadDashboard();

    }

    if (page === "all-rooms") {

        loadRooms();

    }

}
