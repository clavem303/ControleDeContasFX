# ControleDeContas
Controle das contas da casa.

# 📂 Design de Estrutura de Pacotes: Arquitetura em Camadas

O projeto **ControleDeContas** utiliza a **Arquitetura em Camadas (Layered Architecture)**, separando responsabilidades para garantir baixo acoplamento, alta coesão e facilidade de manutenção (Princípios SOLID).

A estrutura de pacotes no diretório `src/main/java/com/controldecontas` segue o padrão abaixo:

src/main/java/tech.claven303 

├── App.java (Classe Principal de Execução)

├── factory/ (Padrões Criacionais - Factory Method)│ 

    └── ContaFactory.java
    
├── model/ (Camada de Domínio/Objetos de Negócio) │

    └── Conta.java │ 
        └── ContaFixa.java │ 
        └── ContaVariavel.java
        
├── service/ (Camada de Serviço/Regras de Negócio/SRP) │

    └── GerenciadorContas.java
    
├── view/ (Camada de Apresentação/Interação com Usuário) 

    └── ConsoleUI.java
    
---

## 🏗️ Detalhamento das Camadas (Pacotes)

Cada pacote representa uma camada lógica com uma responsabilidade bem definida.

### 1. `model` (Camada de Domínio)

Este pacote contém os objetos de negócio (Entidades) e sua lógica interna.

| Conceito POO              | Conteúdo                                  | Responsabilidade |
| :---                      | :---                                      | :--- |
| **Encapsulamento**        | `Conta.java` (Abstrata)                   | Define atributos e o comportamento base (`calcularValorTotal()`). |
| **Herança/Polimorfismo**  | `ContaFixa.java` / `ContaVariavel.java`   | Implementações concretas que estendem `Conta`, fornecendo a lógica de cálculo específica. |

> **Regra:** O `model` é o núcleo e **não deve** ter dependências de `service`, `factory` ou `view`.

### 2. `factory` (Padrões Criacionais)

Esta camada implementa o padrão **Factory Method** para isolar a lógica de criação de objetos.

| Padrão                | Conteúdo              | Responsabilidade |
| :---                  | :---                  | :--- |
| **Factory Method**    | `ContaFactory.java`   | Decidir e instanciar a subclasse de `Conta` correta com base no tipo solicitado. |

> **Benefício:** Se um novo tipo de conta for adicionado, apenas a `ContaFactory` precisa ser alterada (Princípio Open/Closed).

### 3. `service` (Camada de Serviço e Regras de Negócio)

Contém a lógica de negócios e orquestra as operações (o Repositório de dados em memória).

| Princípio | Conteúdo                  | Responsabilidade |
| :---      | :---                      | :--- |
| **SRP**   | `GerenciadorContas.java`  | **Gerenciar** a coleção de contas (Adicionar, Listar, Excluir) e **calcular** totais do sistema. |

### 4. `view` (Camada de Apresentação)

Lida exclusivamente com a interface do usuário (I/O).

| Princípio                     | Conteúdo          | Responsabilidade |
| :---                          | :---              | :--- |
| **Separação de Preocupações** | `ConsoleUI.java`  | Leitura da entrada do usuário (`Scanner`), exibição de menus e formatação da saída no console. |

> **Regra:** O `view` interage com o `service`, mas nunca contém regras de negócio.

### 5. Pacote Raiz (`tech.claven303`)

| Conteúdo      | Responsabilidade |
| :---          | :--- |
| `App.java`    | A classe principal (`main` method). Inicializa o sistema, conecta as instâncias de `service` e `view`, e inicia a execução da aplicação. |

---
