document.addEventListener('DOMContentLoaded', () => {
    const menuOpenButton = document.getElementById('menu-open-button');
    const menuCloseButton = document.getElementById('menu-close-button');
    const mainMenu = document.getElementById('main-menu');
    const menuContainer = document.querySelector('.menu-container');

    const transitionDuration = 300;

    function openMenu() {
        menuOpenButton.style.opacity = '0';
        menuOpenButton.style.visibility = 'hidden';
        menuOpenButton.style.pointerEvents = 'none';

        menuCloseButton.style.opacity = '1';
        menuCloseButton.style.visibility = 'visible';
        menuCloseButton.style.pointerEvents = 'auto';

        mainMenu.classList.remove('menu-hidden-display');
        mainMenu.style.visibility = 'visible';
        mainMenu.style.pointerEvents = 'auto';

        requestAnimationFrame(() => {
            mainMenu.classList.add('visible');
        });
    }

    function closeMenu() {
        menuCloseButton.style.opacity = '0';
        menuCloseButton.style.visibility = 'hidden';
        menuCloseButton.style.pointerEvents = 'none';

        menuOpenButton.style.opacity = '1';
        menuOpenButton.style.visibility = 'visible';
        menuOpenButton.style.pointerEvents = 'auto';

        mainMenu.classList.remove('visible');
        mainMenu.style.pointerEvents = 'none';

        const handleTransitionEnd = (event) => {
            if (event.propertyName === 'max-height' || event.propertyName === 'top') {
                mainMenu.classList.add('menu-hidden-display');
                mainMenu.style.visibility = 'hidden';
                mainMenu.removeEventListener('transitionend', handleTransitionEnd);
            }
        };
        mainMenu.addEventListener('transitionend', handleTransitionEnd);
    }

    if (menuOpenButton && menuCloseButton && mainMenu && menuContainer) {
        menuCloseButton.style.opacity = '0';
        menuCloseButton.style.visibility = 'hidden';
        menuCloseButton.style.pointerEvents = 'none';

        menuOpenButton.addEventListener('click', openMenu);
        menuCloseButton.addEventListener('click', closeMenu);

        document.addEventListener('click', (event) => {
            if (mainMenu.classList.contains('visible') && !menuContainer.contains(event.target)) {
                closeMenu();
            }
        });
    } else {
        console.error("Um ou mais elementos do menu não foram encontrados. Verifique os IDs no HTML.");
    }
});