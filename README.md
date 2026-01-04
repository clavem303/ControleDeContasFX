# 💰 Clavem303 Finanças

**Clavem303 Finanças** é um gerenciador financeiro pessoal desktop, desenvolvido em JavaFX, focado em privacidade e controle total. Ele permite gerenciar receitas, despesas e cartões de crédito de forma visual e intuitiva, sem depender de internet ou nuvem.

![Icone](src/main/resources/tech/clavem303/image/icon.png)

## 🚀 Funcionalidades

* **Dashboard Visual:** Gráficos de consumo vs. investimento e indicadores de saúde financeira.
* **Gestão de Cartões:** Controle de faturas e dias de vencimento.
* **Categorias Personalizáveis:** Crie suas próprias categorias com ícones e cores.
* **Backup Local:** Seus dados ficam salvos em um banco SQLite (`financeiro.db`) na sua máquina.
* **Privacidade:** Funciona 100% offline.

---

## 📦 Instalação (Linux / Ubuntu)

O aplicativo é distribuído como um pacote nativo `.deb`, que já inclui todas as dependências necessárias (não é preciso instalar Java separadamente).

### 1. Instalar
Baixe o arquivo `.deb` gerado e execute o seguinte comando no terminal (na pasta do arquivo):

```bash
sudo dpkg -i clavem303financas_1.0.0_amd64.deb
```

---

🗑️ Desinstalação (Como Remover)
Para desinstalar o aplicativo do sistema, abra o terminal e execute:

```bash
sudo apt remove clavem303financas
```

Se você quiser fazer uma limpeza completa (remover configurações residuais do instalador):

```bash
sudo apt purge clavem303financas
```
