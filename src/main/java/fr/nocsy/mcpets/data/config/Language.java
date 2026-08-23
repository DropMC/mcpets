package fr.nocsy.mcpets.data.config;

import fr.nocsy.mcpets.utils.Utils;
import gg.dropmc.survival.core.api.message.MessageAPI;
import gg.dropmc.survival.core.api.message.MessageType;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Every player facing string, with the tone it is sent in.
 * <p>
 * The tone is not baked into the text. Chat lines go out through core's {@link MessageAPI}, which frames them with
 * the templates every DropMC plugin shares, so this file stays plain sentences and a change to the house style
 * reaches the pets too. {@link MessageType#INFO} is the untouched frame, which is what menu titles, item names and
 * anything else drawn inside a GUI need.
 */
public enum Language {

    INVENTORY_PETS_MENU(MessageType.INFO, "<dark_gray>Bichinhos"),
    INVENTORY_PETS_MENU_INTERACTIONS(MessageType.INFO, "<dark_gray>Bichinho"),
    INVENTORY_MOUNTS_MENU(MessageType.INFO, "<dark_gray>Montarias"),
    INVENTORY_MOUNTS_MENU_INTERACTIONS(MessageType.INFO, "<dark_gray>Montaria"),
    MOUNT_ITEM_NAME(MessageType.INFO, "<white>Montar"),
    MOUNT_ITEM_DESCRIPTION(MessageType.INFO, "<gray>Clique para montar no seu bichinho."),
    RENAME_ITEM_NAME(MessageType.INFO, "<white>Renomear"),
    RENAME_ITEM_DESCRIPTION(MessageType.INFO, "<gray>Clique para dar um nome ao seu bichinho."),
    BACK_TO_PETMENU_ITEM_NAME(MessageType.INFO, "<white>Voltar"),
    BACK_TO_PETMENU_ITEM_DESCRIPTION(MessageType.INFO, "<gray>Clique para voltar ao menu."),
    INVENTORY_ITEM_NAME(MessageType.INFO, "<white>Mochila"),
    INVENTORY_ITEM_DESCRIPTION(MessageType.INFO, "<gray>Clique para abrir a mochila do seu bichinho."),
    SKINS_ITEM_NAME(MessageType.INFO, "<white>Aparências"),
    SKINS_ITEM_DESCRIPTION(MessageType.INFO, "<gray>Clique para trocar a aparência do seu bichinho."),
    EQUIPMENT_ITEM_NAME(MessageType.INFO, "<white>Equipamento"),
    EQUIPMENT_DESCRIPTION(MessageType.INFO, "<gray>Clique para abrir o equipamento do seu bichinho."),
    NEXT_PAGE_ITEM_NAME(MessageType.INFO, "<white>Próxima página <dark_gray>(<gray>%currentPage%<dark_gray>/<gray>%maxPage%<dark_gray>)"),
    NEXT_PAGE_ITEM_DESCRIPTION(MessageType.INFO, "<gray>Clique para avançar."),
    PREVIOUS_PAGE_ITEM_NAME(MessageType.INFO, "<white>Página anterior <dark_gray>(<gray>%currentPage%<dark_gray>/<gray>%maxPage%<dark_gray>)"),
    PREVIOUS_PAGE_ITEM_DESCRIPTION(MessageType.INFO, "<gray>Clique para voltar."),
    NICKNAME(MessageType.INFO, "<gray>Nome: <white>%nickname%"),
    NICKNAME_ITEM_LORE(MessageType.INFO, "<gray>Clique para guardar o seu bichinho."),
    CATEGORY_MENU_TITLE(MessageType.INFO, "<dark_gray>Bichinhos"),
    PET_INVENTORY_TITLE(MessageType.INFO, "<dark_gray>Mochila de %pet%"),
    PET_SKINS_TITLE(MessageType.INFO, "<dark_gray>Aparências de %pet%"),
    PET_STATUS_ALIVE(MessageType.INFO, "<green>Disponível"),
    PET_STATUS_REVOKED(MessageType.INFO, "<red>Indisponível <dark_gray>(%timeleft%s)"),
    PET_STATUS_DEAD(MessageType.INFO, "<red>Ferido <dark_gray>(%timeleft%s)"),
    PET_TAMING_PROGRESS(MessageType.INFO, "<gray>Domesticando: <green>%progress%%</green> %progressbar%"),
    SIGNAL_STICK_SIGNAL(MessageType.INFO, "<gray>Ordem ativa: <white>%signal%"),
    KEY_LIST(MessageType.INFO, "<gray>Chaves disponíveis:"),
    TAG_TO_REMOVE_NAME(MessageType.INFO, "Nenhum"),
    PET_STATS(MessageType.INFO, "<white>Informações do bichinho\n<gray>Situação: %status%\n<gray>Nível: <white>%levelname%\n \n<white>%health%<gray>/<white>%maxhealth% <red>❤\n<gray>Regeneração: <white>%regeneration%</white> ❤/s\n<gray>Dano: <white>%damagemodifier%%\n<gray>Resistência: <white>%resistancemodifier%%\n<gray>Poder: <white>%power%%\n \n<gray>Experiência: <white>%experience%<gray>/<white>%threshold%\n%progressbar%"),
    TYPE_NAME_IN_CHAT(MessageType.INFO, "<gray>Escreva no chat o nome que você quer dar ao seu bichinho."),
    IF_WISH_TO_REMOVE_NAME(MessageType.INFO, "<gray>Para tirar o nome, escreva <white>%tag%</white> no chat."),
    SUMMONED(MessageType.SUCCESS, "Seu bichinho foi chamado."),
    NICKNAME_CHANGED_SUCCESSFULY(MessageType.SUCCESS, "Nome alterado."),
    SKIN_APPLIED(MessageType.SUCCESS, "Aparência alterada."),
    SIGNAL_STICK_GIVEN(MessageType.SUCCESS, "Você recebeu o bastão de ordens. Clique com o direito para dar uma ordem e com o esquerdo para trocar de ordem."),
    PETUNLOCKED(MessageType.SUCCESS, "Você desbloqueou o bichinho <gold>%petName%</gold>."),
    ITEM_UPDATED(MessageType.SUCCESS, "Item atualizado na chave <gold>%key%</gold>."),
    KEY_REMOVED(MessageType.SUCCESS, "Chave removida."),
    KEY_ADDED(MessageType.SUCCESS, "Chave adicionada com o item correspondente."),
    RELOAD_SUCCESS(MessageType.SUCCESS, "Configurações recarregadas."),
    HOW_MANY_PETS_LOADED(MessageType.SUCCESS, "<gold>%numberofpets%</gold> bichinhos registrados."),
    STATS_CLEARED(MessageType.SUCCESS, "Todo o progresso dos bichinhos foi apagado."),
    STATS_CLEARED_FOR_PET_FOR_PLAYER(MessageType.SUCCESS, "O progresso do bichinho <gold>%petId%</gold> de <gold>%player%</gold> foi apagado."),
    STATS_CLEARED_FOR_PET(MessageType.SUCCESS, "O progresso do bichinho <gold>%petId%</gold> foi apagado."),
    DEBUGGER_JOINING(MessageType.SUCCESS, "Modo de depuração ativado."),
    DEBUGGER_LEAVE(MessageType.SUCCESS, "Modo de depuração desativado."),
    REVOKED(MessageType.WARNING, "Seu bichinho foi guardado."),
    REVOKED_FOR_NEW_ONE(MessageType.WARNING, "Seu bichinho anterior foi guardado para chamar o novo."),
    PET_REPLACED_BY_NEW(MessageType.WARNING, "<gold>%oldpet%</gold> foi substituído por <gold>%newpet%</gold>."),
    ALREADY_INSIDE_VEHICULE(MessageType.WARNING, "Você já está montado em algo. Desça antes de usar isso."),
    CANT_FOLLOW_HERE(MessageType.WARNING, "Seu bichinho não pode te acompanhar nesta área."),
    LOOP_SPAWN(MessageType.WARNING, "Seu bichinho foi guardado porque estava com dificuldade para te acompanhar."),
    PET_COULD_NOT_EVOLVE(MessageType.WARNING, "Seu bichinho não evoluiu porque você já tem a evolução dele."),
    PET_STATS_MAX_LEVEL(MessageType.WARNING, "Seu bichinho já está no nível máximo."),
    REVOKED_UNKNOWN(MessageType.ERROR, "Seu bichinho não pôde ser chamado. Avise a equipe se isso continuar acontecendo."),
    MYTHICMOB_NULL(MessageType.ERROR, "Este bichinho não pôde ser chamado."),
    NO_MOB_MATCH(MessageType.ERROR, "Este bichinho não pôde ser chamado."),
    NOT_ALLOWED(MessageType.ERROR, "Você não tem esse bichinho."),
    OWNER_NOT_FOUND(MessageType.ERROR, "Este bichinho não pôde ser chamado."),
    REVOKED_BEFORE_CHANGES(MessageType.ERROR, "Seu bichinho foi guardado antes das mudanças serem aplicadas."),
    NOT_MOUNTABLE(MessageType.ERROR, "Este bichinho não pode ser montado."),
    ALREADY_MOUNTING(MessageType.ERROR, "Você já está montado em algo. Desça antes de tentar de novo."),
    NOT_MOUNTABLE_HERE(MessageType.ERROR, "Você não pode montar nesta área."),
    CANT_MOUNT_PET_YET(MessageType.ERROR, "Você não pode montar neste bichinho."),
    NICKNAME_NOT_CHANGED(MessageType.ERROR, "O nome não pode ficar em branco."),
    PET_DOESNT_EXIST(MessageType.ERROR, "Este bichinho não existe."),
    PLAYER_NOT_CONNECTED(MessageType.ERROR, "O jogador <gold>%player%</gold> não está online."),
    BLACKLISTED_WORD(MessageType.ERROR, "O nome não foi alterado: a palavra <gold>%word%</gold> não é permitida."),
    NO_ACTIVE_PET(MessageType.ERROR, "Você não tem nenhum bichinho ativo."),
    SPECIFY_PET(MessageType.ERROR, "Você tem mais de um bichinho ativo. Diga qual: <gold>%pets%</gold>"),
    REQUIRES_ITEM_IN_HAND(MessageType.ERROR, "Segure um item na mão para atualizá-lo na configuração."),
    ITEM_DOESNT_EXIST(MessageType.ERROR, "A chave <gold>%key%</gold> não existe. Use o argumento <gold>add</gold> para criá-la."),
    KEY_DOESNT_EXIST(MessageType.ERROR, "Esta chave não está registrada."),
    KEY_ALREADY_EXISTS(MessageType.ERROR, "Esta chave já está registrada. Use-a para substituir o item atual."),
    REQUIRES_MODELENGINE(MessageType.ERROR, "Este recurso precisa do ModelEngine e ele não está disponível."),
    USAGE(MessageType.ERROR, "Este comando não existe."),
    MISSING_ARGUMENTS(MessageType.ERROR, "Faltam argumentos. Uso: <gold>%usage%</gold>"),
    INVALID_AMOUNT(MessageType.ERROR, "A quantidade precisa ser um número maior que zero."),
    NO_PERM(MessageType.ERROR, "Você não tem permissão para isso."),
    BLACKLISTED_WORLD(MessageType.ERROR, "Bichinhos não funcionam neste mundo."),
    CATEGORY_DOESNT_EXIST(MessageType.ERROR, "Esta categoria não existe."),
    PET_INVENTORY_COULDNOT_OPEN(MessageType.ERROR, "Esta mochila não pôde ser aberta."),
    SKIN_COULD_NOT_APPLY(MessageType.ERROR, "A aparência não pôde ser aplicada."),
    GLOBAL_RESPAWN_TIMER_RUNNING(MessageType.ERROR, "Espere <gold>%timeLeft%s</gold> para chamar um bichinho."),
    RESPAWN_TIMER_RUNNING(MessageType.ERROR, "Seu bichinho ainda está se recuperando. Espere <gold>%timeLeft%s</gold>."),
    REVOKE_TIMER_RUNNING(MessageType.ERROR, "Seu bichinho ainda está se recuperando. Espere <gold>%timeLeft%s</gold>."),
    PLAYER_OR_PET_DOESNT_EXIST(MessageType.ERROR, "Este bichinho não existe, ou esse jogador nunca entrou no servidor."),
    PETFOOD_DOESNT_EXIST(MessageType.ERROR, "Esta comida de bichinho não existe."),
    PETUNLOCK_NOPERM(MessageType.ERROR, "Você não pode usar este item para desbloquear o bichinho."),
    PETUNLOCKED_ALREADY(MessageType.ERROR, "Você já tem o bichinho <gold>%petName%</gold>."),
    PET_ALREADY_TAMED(MessageType.ERROR, "Este bichinho já está domesticado."),
    PET_DOESNT_EAT(MessageType.ERROR, "Este bichinho não come isso."),
    PET_FOOD_ON_COOLDOWN(MessageType.ERROR, "Seu bichinho só vai comer isso de novo em <gold>%timeleft%s</gold>."),
    PET_STATS_EVOLUTION_ALREADY_OWNED(MessageType.ERROR, "Você já tem essa evolução."),
    MAX_ACTIVE_PETS_REACHED(MessageType.ERROR, "Você chegou ao limite de bichinhos ativos.");

    private final MessageType type;
    private String message;

    Language(final MessageType type, final String message) {
        this.type = type;
        this.message = message;
    }

    public void reload() {
        if (LanguageConfig.getInstance().getMap().containsKey(name().toLowerCase())) {
            message = LanguageConfig.getInstance().getMap().get(name().toLowerCase());
        }
    }

    public String getMessage() {
        return Utils.applyPlaceholders(null, message);
    }

    public String getMessagePAPI() {
        return getMessage();
    }

    public Component getComponent() {
        return Utils.toComponent(getMessage());
    }

    public Component getComponentWithPrefix() {
        return getComponent();
    }

    public void sendMessage(final Player player) {
        sendMessage((CommandSender) player);
    }

    public void sendMessage(final CommandSender sender) {
        send(sender, getComponent());
    }

    public void sendMessageFormatted(final CommandSender sender, final FormatArg... args) {
        send(sender, getComponentFormatted(args));
    }

    /**
     * Frames the line in the tone this message carries and sends it. An empty message is a message an admin turned
     * off, so nothing is sent at all.
     */
    private void send(final CommandSender sender, final Component component) {
        if (message.isEmpty()) {
            return;
        }
        switch (type) {
            case SUCCESS -> MessageAPI.success(sender, component).send();
            case WARNING -> MessageAPI.warning(sender, component).send();
            case DANGER -> MessageAPI.danger(sender, component).send();
            case ERROR -> MessageAPI.error(sender, component).send();
            case INFO -> MessageAPI.info(sender, component).send();
        }
    }

    public String getMessageFormatted(final FormatArg... args) {
        String toSend = getMessage();
        for (final FormatArg arg : args) {
            toSend = arg.applyToString(toSend);
        }
        return toSend;
    }

    public Component getComponentFormatted(final FormatArg... args) {
        return Utils.toComponent(getMessageFormatted(args));
    }
}
