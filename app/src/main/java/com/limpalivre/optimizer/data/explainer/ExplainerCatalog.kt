package com.limpalivre.optimizer.data.explainer

import android.content.pm.ApplicationInfo
import com.limpalivre.optimizer.data.model.AppCategory
import com.limpalivre.optimizer.data.model.AppExplanation
import com.limpalivre.optimizer.data.model.CleanActionStatus
import com.limpalivre.optimizer.data.model.DeepCategoryType
import com.limpalivre.optimizer.data.model.RiskLevel

data class DeepCategoryCopy(
    val title: String,
    val subtitle: String,
    val whatItIs: String,
    val willItCauseProblems: String,
    val riskLevel: RiskLevel,
    val defaultSelected: Boolean
)

object ExplainerCatalog {

    private val knownPackages: Map<String, Pair<AppCategory, AppExplanation>> = mapOf(
        "com.whatsapp" to (AppCategory.MESSAGING to AppExplanation(
            whatIsInCache = "Miniaturas temporárias de fotos, figurinhas pré-carregadas, áudios em buffer e previews de links.",
            willItCauseProblems = "Não apaga conversas, grupos, fotos da galeria nem desloga sua conta. O único efeito é recarregar miniaturas antigas quando você rolar chats muito antigos.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Limpeza segura: suas mensagens e mídias salvas permanecem intactas."
        )),
        "com.whatsapp.w4b" to (AppCategory.MESSAGING to AppExplanation(
            whatIsInCache = "Miniaturas temporárias de catálogos, imagens e previews do WhatsApp Business.",
            willItCauseProblems = "Não remove conversas comerciais, clientes nem catálogos salvos na conta.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Totalmente seguro para liberar espaço rapidamente."
        )),
        "org.telegram.messenger" to (AppCategory.MESSAGING to AppExplanation(
            whatIsInCache = "Mídias assistidas em canais/grupos e previews mantidos localmente para abrir instantaneamente.",
            willItCauseProblems = "Não causa problemas: tudo no Telegram fica na nuvem e pode ser baixado novamente se você abrir a conversa outra vez.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Um dos maiores acumuladores de espaço — seguro de limpar."
        )),
        "com.instagram.android" to (AppCategory.SOCIAL to AppExplanation(
            whatIsInCache = "Vídeos de Reels pré-carregados, imagens do feed, filtros e arquivos temporários do navegador interno.",
            willItCauseProblems = "Não desloga sua conta nem apaga publicações, rascunhos da nuvem ou mensagens do Direct. Na primeira abertura, o feed recarrega normalmente.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Recomendado limpar com frequência; costuma acumular centenas de MB."
        )),
        "com.zhiliaoapp.musically" to (AppCategory.SOCIAL to AppExplanation(
            whatIsInCache = "Vídeos do TikTok baixados antecipadamente, efeitos de câmera temporários e miniaturas de perfis.",
            willItCauseProblems = "Não desloga nem remove vídeos curtidos/salvos na conta. Atenção apenas se você tiver rascunhos locais ainda não publicados (o cache não apaga rascunhos, mas evite limpar dados).",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Seguro: apenas cache de reprodução é removido."
        )),
        "com.ss.android.ugc.trill" to (AppCategory.SOCIAL to AppExplanation(
            whatIsInCache = "Vídeos pré-carregados do TikTok e arquivos temporários de efeitos.",
            willItCauseProblems = "Não desloga nem apaga sua conta ou favoritos.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Seguro para limpeza frequente."
        )),
        "com.facebook.katana" to (AppCategory.SOCIAL to AppExplanation(
            whatIsInCache = "Imagens do feed, vídeos pré-carregados, fontes e dados temporários de páginas abertas.",
            willItCauseProblems = "Não encerra sua sessão nem apaga fotos postadas. O feed apenas busca os cards novamente pela internet.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Limpeza segura e com alto ganho de espaço."
        )),
        "com.google.android.youtube" to (AppCategory.STREAMING to AppExplanation(
            whatIsInCache = "Miniaturas de vídeos, trechos de buffer de reprodução e listas temporárias de sugestões.",
            willItCauseProblems = "Não remove inscrições, histórico nem downloads offline salvos no YouTube Premium. Na próxima abertura, as capas recarregam em segundos.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "100% seguro."
        )),
        "com.spotify.music" to (AppCategory.STREAMING to AppExplanation(
            whatIsInCache = "Capas de álbuns, trechos de músicas tocadas recentemente em streaming e podcasts em buffer.",
            willItCauseProblems = "Não apaga playlists nem músicas baixadas para ouvir offline (se você limpa apenas o cache). Pode aumentar levemente o uso de dados ao reproduzir faixas antigas em streaming.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Seguro; libera bastante espaço após semanas de uso."
        )),
        "com.netflix.mediaclient" to (AppCategory.STREAMING to AppExplanation(
            whatIsInCache = "Capas de séries, trailers pré-carregados e dados temporários de navegação do catálogo.",
            willItCauseProblems = "Não desconecta sua conta nem apaga perfis. Apenas trailers e capas são baixados de novo.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Seguro para limpar."
        )),
        "com.android.chrome" to (AppCategory.BROWSER to AppExplanation(
            whatIsInCache = "Imagens, scripts, estilos de sites visitados e arquivos temporários de páginas da web.",
            willItCauseProblems = "Não apaga senhas salvas, favoritos, histórico nem abas abertas. Em alguns sites, o próximo carregamento pode levar meio segundo a mais enquanto baixa as imagens de novo.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Seguro e recomendado; cookies de login ficam nos Dados, não no Cache."
        )),
        "org.mozilla.firefox" to (AppCategory.BROWSER to AppExplanation(
            whatIsInCache = "Arquivos temporários de páginas web, scripts e imagens em cache do navegador.",
            willItCauseProblems = "Favoritos, senhas e abas permanecem intactos.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Limpeza segura."
        )),
        "com.microsoft.emmx" to (AppCategory.BROWSER to AppExplanation(
            whatIsInCache = "Cache de páginas, imagens e recursos temporários do Edge.",
            willItCauseProblems = "Senhas, favoritos e histórico continuam salvos.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Limpeza segura."
        )),
        "com.shopee.br" to (AppCategory.SHOPPING to AppExplanation(
            whatIsInCache = "Fotos de anúncios, banners promocionais e animações temporárias de lojas.",
            willItCauseProblems = "Não remove carrinho, pedidos nem login. Apenas as fotos dos produtos carregam novamente.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Apps de compras acumulam centenas de MB em fotos de catálogo — seguro limpar."
        )),
        "com.mercadolibre" to (AppCategory.SHOPPING to AppExplanation(
            whatIsInCache = "Imagens de produtos visualizados recentemente e recursos da interface.",
            willItCauseProblems = "Não altera compras, favoritos nem sua conta do Mercado Livre/Pago.",
            riskLevel = RiskLevel.SAFE,
            safetyTip = "Totalmente seguro."
        ))
    )

    fun classifyApp(packageName: String, androidCategory: Int): AppCategory {
        knownPackages[packageName]?.let { return it.first }
        val lower = packageName.lowercase()
        return when {
            lower.contains("bank") || lower.contains("nubank") || lower.contains("inter") ||
                lower.contains("itau") || lower.contains("bradesco") || lower.contains("santander") ||
                lower.contains("caixa") || lower.contains("picpay") || lower.contains("mercadopago") -> AppCategory.BANKING
            lower.contains("browser") || lower.contains("chrome") || lower.contains("opera") ||
                lower.contains("brave") || lower.contains("webview") -> AppCategory.BROWSER
            lower.contains("whatsapp") || lower.contains("telegram") || lower.contains("discord") ||
                lower.contains("signal") || lower.contains("messenger") -> AppCategory.MESSAGING
            lower.contains("shop") || lower.contains("amazon") || lower.contains("shein") ||
                lower.contains("aliexpress") || lower.contains("magalu") -> AppCategory.SHOPPING
            androidCategory == ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMING
            androidCategory == ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            androidCategory == ApplicationInfo.CATEGORY_VIDEO ||
                androidCategory == ApplicationInfo.CATEGORY_AUDIO -> AppCategory.STREAMING
            androidCategory == ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            else -> AppCategory.UTILITY
        }
    }

    fun explainApp(packageName: String, appName: String, androidCategory: Int): AppExplanation {
        knownPackages[packageName]?.let { return it.second }
        val category = classifyApp(packageName, androidCategory)
        return when (category) {
            AppCategory.SOCIAL -> AppExplanation(
                whatIsInCache = "Miniaturas de publicações, vídeos pré-carregados e dados temporários de feed do $appName.",
                willItCauseProblems = "Não desloga sua conta nem remove postagens. A única diferença é recarregar miniaturas antigas quando você abrir o app.",
                riskLevel = RiskLevel.SAFE,
                safetyTip = "Limpeza segura."
            )
            AppCategory.BROWSER -> AppExplanation(
                whatIsInCache = "Imagens, estilos CSS e scripts temporários de sites visitados no $appName.",
                willItCauseProblems = "Não apaga senhas, favoritos ou logins. Sites visitados apenas baixam as imagens novamente no próximo acesso.",
                riskLevel = RiskLevel.SAFE,
                safetyTip = "Limpeza segura."
            )
            AppCategory.STREAMING -> AppExplanation(
                whatIsInCache = "Capas, trechos em buffer e previews temporários de mídia no $appName.",
                willItCauseProblems = "Não apaga sua conta nem listas salvas. Downloads offline protegidos permanecem nos Dados do app.",
                riskLevel = RiskLevel.SAFE,
                safetyTip = "Limpeza segura."
            )
            AppCategory.MESSAGING -> AppExplanation(
                whatIsInCache = "Miniaturas temporárias de imagens, figurinhas em cache e previews de conversa no $appName.",
                willItCauseProblems = "Não apaga mensagens nem sua conta. Apenas miniaturas de chats antigos podem ser recarregadas ao rolar.",
                riskLevel = RiskLevel.SAFE,
                safetyTip = "Limpeza segura."
            )
            AppCategory.GAMING -> AppExplanation(
                whatIsInCache = "Shaders pré-compilados, banners de eventos e arquivos temporários de atualização do $appName.",
                willItCauseProblems = "Não apaga seu progresso nem sua conta. Na primeira partida após limpar, o jogo pode levar alguns segundos extras para recriar shaders ou carregar telas iniciais.",
                riskLevel = RiskLevel.CAUTION,
                safetyTip = "Seguro para o progresso, mas pode exigir recarregamento curto ao abrir o jogo."
            )
            AppCategory.SHOPPING -> AppExplanation(
                whatIsInCache = "Fotos de produtos visualizados e banners de promoções no $appName.",
                willItCauseProblems = "Não apaga carrinho, pedidos ou login.",
                riskLevel = RiskLevel.SAFE,
                safetyTip = "Limpeza segura com bom ganho de espaço."
            )
            AppCategory.BANKING -> AppExplanation(
                whatIsInCache = "Comprovantes em visualização temporária, ícones e arquivos temporários do $appName.",
                willItCauseProblems = "Limpar cache não desvincula token bancário (que fica nos Dados do app). Mesmo assim, por cautela, aplicativos bancários vêm desmarcados por padrão.",
                riskLevel = RiskLevel.CAUTION,
                safetyTip = "Desmarcado por padrão; limpe somente se desejar."
            )
            AppCategory.PRODUCTIVITY -> AppExplanation(
                whatIsInCache = "Previews locais de documentos, miniaturas de PDFs e buffers temporários do $appName.",
                willItCauseProblems = "Não apaga documentos salvos ou contas conectadas. Arquivos abertos da nuvem apenas baixam a visualização novamente quando você abri-los.",
                riskLevel = RiskLevel.SAFE,
                safetyTip = "Limpeza segura."
            )
            AppCategory.UTILITY -> AppExplanation(
                whatIsInCache = "Arquivos temporários criados pelo $appName para agilizar o carregamento de telas e imagens.",
                willItCauseProblems = "Em regra não causa nenhum problema: configurações pessoais e logins ficam nos Dados do app, e o cache é recriado automaticamente se precisar.",
                riskLevel = RiskLevel.SAFE,
                safetyTip = "Seguro para limpar sempre que acumular espaço."
            )
        }
    }

    fun explainDeepCategory(type: DeepCategoryType): DeepCategoryCopy {
        return when (type) {
            DeepCategoryType.TEMP_LOGS -> DeepCategoryCopy(
                title = "Temporários e Registros (.tmp / .log)",
                subtitle = "Arquivos de diagnóstico e rascunhos abandonados no armazenamento",
                whatItIs = "Registros técnicos (.log, .dmp, .crash) e arquivos temporários (.tmp, .temp, .chk) deixados para trás por aplicativos ou atualizações.",
                willItCauseProblems = "Não causa problema algum. Esses arquivos servem apenas para depuração momentânea e o sistema recria novos registros se precisar.",
                riskLevel = RiskLevel.SAFE,
                defaultSelected = true
            )
            DeepCategoryType.THUMBNAILS_CACHE -> DeepCategoryCopy(
                title = "Miniaturas Ocultas (.thumbnails)",
                subtitle = "Cache antigo de pré-visualizações da galeria e pastas de mídia",
                whatItIs = "Cópias pequenas geradas pela Galeria e reprodutores para exibir miniaturas de fotos/vídeos rapidamente — muitas vezes continuam ocupando gigabytes mesmo depois que você apagou as fotos originais.",
                willItCauseProblems = "Não apaga suas fotos nem vídeos originais! Na próxima vez que abrir a Galeria, ela gera apenas as miniaturas das fotos que ainda existem.",
                riskLevel = RiskLevel.SAFE,
                defaultSelected = true
            )
            DeepCategoryType.EMPTY_RESIDUAL -> DeepCategoryCopy(
                title = "Pastas Vazias e Resíduos",
                subtitle = "Diretórios vazios deixados por aplicativos desinstalados",
                whatItIs = "Pastas sem nenhum arquivo dentro que sobraram na memória interna após apagar ou desinstalar apps antigos.",
                willItCauseProblems = "Nenhum problema. Pastas de sistema protegidas (DCIM, Download, Music, Pictures, Documents, Android) nunca são removidas.",
                riskLevel = RiskLevel.SAFE,
                defaultSelected = true
            )
            DeepCategoryType.MESSENGER_CACHE -> DeepCategoryCopy(
                title = "Cache Profundo de Mensageiros",
                subtitle = "Arquivos temporários ocultos de WhatsApp / Telegram e backups antigos",
                whatItIs = "Subpastas temporárias ocultas (como .Shared, .Statuses e cópias de backup de banco de dados mais antigas mantendo sempre o backup mais recente).",
                willItCauseProblems = "Não apaga conversas nem suas fotos da galeria. Status visualizados recentemente somem apenas do cache oculto e backups extras antigos são removidos mantendo sempre o último.",
                riskLevel = RiskLevel.CAUTION,
                defaultSelected = false
            )
            DeepCategoryType.DUPLICATES -> DeepCategoryCopy(
                title = "Arquivos Duplicados Exatos",
                subtitle = "Cópias idênticas (mesmo conteúdo SHA-256) ocupando espaço em dobro",
                whatItIs = "Arquivos baixados ou recebidos duas ou mais vezes com conteúdo 100% idêntico em pastas diferentes. O arquivo original mais antigo é bloqueado automaticamente contra exclusão.",
                willItCauseProblems = "Seguro se mantiver apenas os marcados automaticamente: o original mais antigo fica protegido e apenas as cópias extras são removidas.",
                riskLevel = RiskLevel.CAUTION,
                defaultSelected = false
            )
            DeepCategoryType.APK_INSTALLERS -> DeepCategoryCopy(
                title = "Instaladores APK Obsoletos",
                subtitle = "Arquivos .apk, .apks e .xapk baixados anteriormente",
                whatItIs = "Pacotes de instalação salvos geralmente na pasta Download após instalar algum aplicativo manualmente.",
                willItCauseProblems = "Não desinstala aplicativos que já estão instalados no celular! Só apague se você não precisar mais guardar o instalador para reinstalar sem internet.",
                riskLevel = RiskLevel.CAUTION,
                defaultSelected = false
            )
            DeepCategoryType.LARGE_FILES -> DeepCategoryCopy(
                title = "Arquivos Grandes (+50 MB)",
                subtitle = "Revisão manual de vídeos, ZIPs e arquivos pesados",
                whatItIs = "Lista os maiores arquivos encontrados no armazenamento interno (acima de 50 MB) para você decidir o que realmente não precisa mais.",
                willItCauseProblems = "Atenção: apagar vídeos pessoais ou downloads importantes aqui é permanente. Nenhum item vem marcado — você escolhe manualmente o que apagar.",
                riskLevel = RiskLevel.MANUAL,
                defaultSelected = false
            )
        }
    }

    fun buildPostCleanAssessment(status: CleanActionStatus, title: String, freedText: String): Pair<String, String> {
        return when (status) {
            CleanActionStatus.SUCCESS -> Pair(
                "Cache/arquivos temporários de $title removidos ($freedText liberados).",
                "Sem problemas: logins, conversas e arquivos pessoais foram preservados. Apenas arquivos temporários foram limpos."
            )
            CleanActionStatus.ALREADY_CLEAN -> Pair(
                "O cache de $title já estava zerado ou abaixo do mínimo no sistema.",
                "Sem problemas: nenhuma ação destrutiva foi necessária."
            )
            CleanActionStatus.PARTIAL -> Pair(
                "Limpeza parcial realizada em $title ($freedText liberados); alguns arquivos estavam em uso ou protegidos pelo Android.",
                "Sem problemas no aparelho: os arquivos bloqueados foram ignorados com segurança."
            )
            CleanActionStatus.SKIPPED -> Pair(
                "$title foi pulado por proteção ou cancelamento.",
                "Nenhuma alteração realizada neste item."
            )
            CleanActionStatus.MANUAL_REQUIRED -> Pair(
                "O sistema do fabricante não expôs o botão padrão automaticamente para $title.",
                "Sem risco: você pode tocar em 'Abrir config' na lista para limpar o cache manualmente em 1 toque."
            )
            CleanActionStatus.FAILED -> Pair(
                "Não foi possível remover $title devido a restrição de permissão ou bloqueio do sistema.",
                "O arquivo original permaneceu intacto sem causar falhas."
            )
        }
    }
}
