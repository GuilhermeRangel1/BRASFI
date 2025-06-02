document.addEventListener("DOMContentLoaded", () => {
  const toggleBtn = document.getElementById("menuToggle");
  const menu = document.getElementById("menu");

  toggleBtn.addEventListener("click", () => {
    menu.classList.toggle("visible");
    toggleBtn.classList.toggle("rotated");
  });
});
