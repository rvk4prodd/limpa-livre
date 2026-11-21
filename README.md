# LimpaLivre — Otimizador Android 100% Gratuito

O **LimpaLivre** é um aplicativo de limpeza e otimização de celular Android construído para oferecer os mesmos recursos práticos de otimizadores pagos — **sem assinaturas, sem anúncios, sem paywall e sem conexão com a internet**.

## Baixar no Celular (APK)
1. Acesse a aba **Releases** aqui no repositório do GitHub.
2. Baixe o arquivo **`LimpaLivre-v1.0.0-release.apk`**.
3. No Android, autorize a instalação de fontes desconhecidas pelo seu navegador/gerenciador de arquivos e conclua a instalação.

> **Compilação automática na nuvem:** Todo push na branch `main` roda os testes unitários e gera automaticamente os APKs atualizados via [GitHub Actions](.github/workflows/build-apk.yml).

---

## Principais Recursos

- **Limpeza de Cache dos Aplicativos (Selecionável + Explicação Individual)**
  - Mede o tamanho real do cache de cada app via `StorageStatsManager`.
  - Permite selecionar exatamente quais apps limpar, ordenar por maior cache ou buscar por nome.
  - Explica para **cada aplicativo** o que fica armazenado no cache dele e se limpar causa algum problema (ex.: logins e conversas continuam intactos).
  - Suporta **Modo Automático em 1 toque** (via Acessibilidade segura que *nunca* clica em "Limpar dados") e **Modo Guiado Manual** (sem precisar ativar Acessibilidade).
- **Limpeza Profunda do Armazenamento**
  - Varre arquivos temporários e logs (`.tmp`, `.temp`, `.log`, `.dmp`, `.crash`).
  - Encontra miniaturas ocultas antigas (`.thumbnails`) que permanecem após apagar fotos.
  - Detecta pastas vazias residuais de aplicativos removidos.
  - Identifica cache profundo de mensageiros (`WhatsApp/.Shared`, `.Statuses`, backups antigos mantendo o mais recente protegido).
  - **Detector de Arquivos Duplicados Exatos (SHA-256)** protegendo o arquivo original mais antigo automaticamente.
  - Lista instaladores `.apk` obsoletos e arquivos grandes (+50 MB) para revisão manual.
- **Otimizador de RAM, Gerenciador de Apps e Saúde da Bateria**
  - Encerra processos ociosos em segundo plano selecionados e mede a RAM recuperada.
  - Filtra apps mais pesados, pouco usados (30+ dias) e apps com permissões sensíveis (Câmera, Microfone, Localização, Contatos, SMS, Chamadas).
  - Mostra temperatura, voltagem, fonte de carregamento, saúde da bateria e especificações de hardware.
- **Relatório Pós-Limpeza & Histórico**
  - Após cada limpeza, mostra o quanto foi liberado, o que foi feito em cada item e confirma se houve algum problema.

---

## Permissões Explicadas (e Dica para Android 13+)

1. **Acesso ao Uso (`PACKAGE_USAGE_STATS`)**: lê o tamanho real de cache/dados de cada aplicativo e há quanto tempo você não abre um app.
2. **Acesso a Todos os Arquivos (`MANAGE_EXTERNAL_STORAGE` / Storage)**: necessário para localizar arquivos temporários, `.thumbnails`, pastas vazias, duplicados e instaladores `.apk`. Pastas protegidas (`DCIM/Camera`, `Android/obb`, `Download`, etc.) nunca são apagadas em lote sem sua seleção explícita.
3. **Acessibilidade (Opcional)**: permite automatizar o clique em **Armazenamento → Limpar cache** nas Configurações do sistema para limpar vários apps seguidos sem ter que tocar um por um.
   - **Nota sobre Android 13, 14, 15 e 16:** ao instalar APKs fora da Play Store, o Android exibe *"Configuração restrita"* na tela de Acessibilidade. Basta abrir **Configurações → Apps → LimpaLivre → menu ⋮ (canto superior direito) → Permitir configurações restritas**, e depois voltar em Acessibilidade para ativar.
4. **Privacidade absoluta:** o aplicativo **não declara `android.permission.INTERNET`** no `AndroidManifest.xml`. Nenhum dado sai do aparelho.
