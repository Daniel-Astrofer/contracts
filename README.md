<!--
status: active
audience: internal
owner: contracts
source_of_truth: contracts schemas, build files and compatibility tests
last_reviewed: 2026-10-04
-->

# Kerosene Contracts

Contratos de comunicação (*wire contracts*), especificações de esquemas e vetores de teste determinísticos compartilhados entre os serviços do ecossistema Kerosene (`discoveryng-node`, `vault`, `krinse-engine`, `users-authentication` e `server-administration`).

## Estrutura do Repositório

```text
contracts/
├── build.gradle.kts         # Build Java 21 com publicação de jars, sources e javadocs
├── Cargo.toml               # Cargo workspace raiz
├── compatibility/           # Regras de compatibilidade do componente (kerosene.json)
├── rust/
│   └── kerosene-contracts/  # Crate Rust com módulos modulares (admin, discovery, ledger, canonical)
│       ├── src/             # Implementação canônica dos contratos
│       └── tests/           # Testes de integração e Known Answer Tests (KAT)
├── schemas/                 # JSON Schemas (Draft 2020-12) versionados por domínio
│   ├── admin/               # Status, ledger, p2p, onramp, reconciliação e provedores
│   ├── discovery/           # Trust bundles, peer hello, admissão e membership manifests
│   └── financial/           # Aprovação de pagamentos
├── src/
│   ├── main/java/           # Biblioteca Java dos contratos (records, DTOs e SPIs)
│   │   ├── com/kerosene/common/financial/
│   │   │   ├── model/       # Primitivos de domínio e value objects (Bitcoin, hashes, satoshis)
│   │   │   ├── approval/    # Protocolos de aprovação de pagamentos e multifator (MFA/Passkeys)
│   │   │   ├── notification/# Eventos assíncronos e auditoria de notificações financeiras
│   │   │   ├── stomp/       # Envelopes e rotas tipadas para relay STOMP de usuário
│   │   │   └── operations/  # Portas de administração operacional, rail health e provisionamento
│   │   ├── com/kerosene/common/vaultmesh/
│   │   │   ├── intent/      # Intenções de liquidação, reservas e autorização híbrida (PQ)
│   │   │   ├── settlement/  # Coordenação de assinatura PSBT e descritores de depósito
│   │   │   └── governance/  # Avanço de épocas diárias e cerimônias de reshare MPC
│   │   └── io/kerosene/contracts/admin/ # Wire models administrativos do ecossistema
│   └── test/java/           # Testes JSON e KAT sincronizados com os vetores em Rust
└── test-vectors/            # Vetores de teste canônicos (.json) compartilhados entre Rust e Java
```

## Como Executar os Testes

### Java
```bash
./gradlew test
./gradlew build
```

### Rust
```bash
cargo test
cargo clippy -- -D warnings
cargo fmt --check
```

## Documentação Global

Arquitetura transversal, regras de negócio compartilhadas e infraestrutura/operação global estão no repositório externo [kerosene-global-docs](../../kerosene-global-docs/README.md). A documentação inline de implementação permanece junto ao código neste repositório.
