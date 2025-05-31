document.addEventListener('DOMContentLoaded', () => {
    const menuToggleButton = document.getElementById('menu-toggle-button');
    const mainMenu = document.getElementById('main-menu');

    // Define a duração da transição manualmente (em milissegundos)
    const transitionDuration = 300;

    if (menuToggleButton && mainMenu) {
        menuToggleButton.addEventListener('click', () => {
            const isMenuOpen = mainMenu.classList.contains('visible');

            mainMenu.classList.toggle('visible');
            menuToggleButton.classList.toggle('rotated');

            if (isMenuOpen) {
                mainMenu.style.pointerEvents = 'none';
                mainMenu.style.visibility = 'hidden'; 

                setTimeout(() => {
                    mainMenu.classList.add('menu-hidden-display');
                }, transitionDuration); 
            } else {
                mainMenu.classList.remove('menu-hidden-display');
                mainMenu.style.visibility = 'visible';
                mainMenu.style.pointerEvents = 'auto';
            }
        });

        document.addEventListener('click', (event) => {
            if (!menuToggleButton.contains(event.target) && !mainMenu.contains(event.target) && mainMenu.classList.contains('visible')) {
                mainMenu.classList.remove('visible');
                menuToggleButton.classList.remove('rotated');

                mainMenu.style.pointerEvents = 'none';
                mainMenu.style.visibility = 'hidden';

                setTimeout(() => {
                    mainMenu.classList.add('menu-hidden-display');
                }, transitionDuration);
            }
        });
    } else {
        console.error("Um ou mais elementos do menu não foram encontrados. Verifique os IDs/Classes no HTML ou o caminho do JS.");
    }
});
