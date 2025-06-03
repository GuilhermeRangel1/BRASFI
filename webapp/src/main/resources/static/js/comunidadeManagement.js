// Função para abrir o modal de criação de comunidade
function OpenForm() {
    document.getElementById("criar-comunidade").style.display = "flex"; // Use 'flex' para centralizar
}

// Função para fechar o modal de criação de comunidade
function CloseForm() {
    document.getElementById("criar-comunidade").style.display = "none";
}

// NOVA: Função para abrir o modal de apagar comunidade
function OpenDeleteCommunityModal() {
    document.getElementById("apagar-comunidade-modal").style.display = "flex";
    loadCommunitiesForDeletion(); // Carrega as comunidades no modal
}

// NOVA: Função para fechar o modal de apagar comunidade
function CloseDeleteCommunityModal() {
    document.getElementById("apagar-comunidade-modal").style.display = "none";
}

// NOVA: Variável para armazenar o ID da comunidade selecionada para exclusão
let selectedCommunityIdForDeletion = null;

// NOVA: Função para carregar comunidades no modal de apagar
async function loadCommunitiesForDeletion() {
    const deleteCommunityList = document.getElementById('deleteCommunityList');
    deleteCommunityList.innerHTML = '<li style="color: #386D6B; font-family: \'Louis George Cafe\', sans-serif;">Carregando comunidades...</li>';
    selectedCommunityIdForDeletion = null; // Resetar a seleção ao abrir o modal
    document.getElementById('confirmDeleteCommunityButton').disabled = true; // Desabilita o botão inicialmente

    try {
        // Requisição para buscar todas as comunidades do novo endpoint REST
        const response = await fetch('/api/comunidades');
        if (!response.ok) {
            throw new Error('Erro ao carregar comunidades: ' + response.statusText);
        }
        const comunidades = await response.json();

        deleteCommunityList.innerHTML = ''; 

        if (comunidades.length === 0) {
            deleteCommunityList.innerHTML = '<li style="color: #386D6B; font-family: \'Louis George Cafe\', sans-serif;">Nenhuma comunidade encontrada.</li>';
            document.getElementById('confirmDeleteCommunityButton').disabled = true;
            return;
        }

        comunidades.forEach(com => {
            const li = document.createElement('li');
            li.textContent = com.nome;
            li.dataset.id = com.id; 
            li.addEventListener('click', () => {
                Array.from(deleteCommunityList.children).forEach(item => {
                    item.classList.remove('selected');
                });
                li.classList.add('selected');
                selectedCommunityIdForDeletion = com.id; 
                document.getElementById('confirmDeleteCommunityButton').disabled = false;
            });
            deleteCommunityList.appendChild(li);
        });
    } catch (error) {
        console.error('Erro ao carregar comunidades para exclusão:', error);
        deleteCommunityList.innerHTML = '<li style="color: #d9534f; font-family: \'Louis George Cafe\', sans-serif;">Erro ao carregar comunidades.</li>';
        document.getElementById('confirmDeleteCommunityButton').disabled = true;
    }
}


document.addEventListener('DOMContentLoaded', () => {
    const editButton = document.getElementById('editCommunitiesButton');
    const communityListSection = document.getElementById('communityListSection');
    const communityManagementOptions = document.getElementById('communityManagementOptions');
    const createCommunityLink = document.getElementById('createCommunityLink');
    const deleteCommunityLink = document.getElementById('deleteCommunityLink');
    const backToCommunitiesLink = document.getElementById('backToCommunitiesLink');
    const closeCreateCommunityModalButton = document.getElementById('closeCreateCommunityModal');

    const apagarComunidadeModal = document.getElementById('apagar-comunidade-modal');
    const closeDeleteCommunityModalButton = document.getElementById('closeDeleteCommunityModal');
    const confirmDeleteCommunityButton = document.getElementById('confirmDeleteCommunityButton');


    if (editButton && communityListSection && communityManagementOptions) {
        editButton.addEventListener('click', (e) => {
            e.preventDefault();
            communityListSection.style.display = 'none';
            communityManagementOptions.style.display = 'block';
        });

        if (createCommunityLink) {
            createCommunityLink.addEventListener('click', (e) => {
                e.preventDefault();
                OpenForm();
            });
        }

        if (deleteCommunityLink) {
            deleteCommunityLink.addEventListener('click', (e) => {
                e.preventDefault();
                OpenDeleteCommunityModal(); 
            });
        }

        if (backToCommunitiesLink) {
            backToCommunitiesLink.addEventListener('click', (e) => {
                e.preventDefault();
                communityManagementOptions.style.display = 'none';
                communityListSection.style.display = 'block';
            });
        }

        if (closeCreateCommunityModalButton) {
            closeCreateCommunityModalButton.addEventListener('click', () => {
                CloseForm();
            });
        }

        if (closeDeleteCommunityModalButton) {
            closeDeleteCommunityModalButton.addEventListener('click', () => {
                CloseDeleteCommunityModal();
            });
        }

        if (confirmDeleteCommunityButton) {
            confirmDeleteCommunityButton.addEventListener('click', async () => {
                if (selectedCommunityIdForDeletion) {
                    if (confirm(`Tem certeza que deseja apagar a comunidade selecionada (ID: ${selectedCommunityIdForDeletion})? Esta ação é irreversível.`)) {
                        try {
                            const response = await fetch(`/comunidades/${selectedCommunityIdForDeletion}`, {
                                method: 'DELETE'
                            });

                            if (response.ok) {
                                alert('Comunidade apagada com sucesso!');
                                CloseDeleteCommunityModal(); 
                                window.location.href = '/comunidades';
                            } else {
                                const errorText = await response.text();
                                throw new Error(errorText || 'Erro desconhecido ao apagar a comunidade.');
                            }
                        } catch (error) {
                            console.error('Erro ao apagar comunidade:', error);
                            alert(`Não foi possível apagar a comunidade: ${error.message}`);
                        }
                    }
                } else {
                    alert('Por favor, selecione uma comunidade para apagar.');
                }
            });
        }
    }
});