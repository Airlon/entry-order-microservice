package br.com.repassa.exception;

import br.com.backoffice_repassa_utils_lib.error.interfaces.RepassaUtilError;

public class EntryOrderError implements RepassaUtilError {
    private static final String APP_PREFIX = "entryOrder";
    private final String errorCode;
    private final String errorMessage;

    public static final RepassaUtilError ENDPOINT_NAO_VALIDO =
            new EntryOrderError("001", "Endpoint não válido.");

    public static final RepassaUtilError TIPO_INVALIDO =
            new EntryOrderError("002", "Tipo da ordem de entrada inválido.");

    public static final RepassaUtilError CLASSIFICACAO_INVALIDA =
            new EntryOrderError("003", "Classificação da ordem de entrada inválida.");

    public static final RepassaUtilError ERRO_AO_CONECTAR_NO_BANCO =
            new EntryOrderError("004", "Erro ao conectar no banco.");

    public static final RepassaUtilError CODIGO_ORDEM_DE_ENTRADA_JA_EXISTE =
            new EntryOrderError("005", "O código da ordem de entrada já existe, por favor tente novamente.");

    public static final RepassaUtilError ID_PRODUTO_NAO_ENCONTRADO =
            new EntryOrderError("006", "Este ID é inválido ou não foi encontrado. Verifique o código e tente novamente.");

    public static final RepassaUtilError PRODUTO_NAO_FINALIZADO =
            new EntryOrderError("007", "O produto associado a este ID não possui cadastro finalizado. Entre em contato com o departamento responsável para atualizar as informações.");

    public static final RepassaUtilError ID_PRODUTO_ORDEM_DE_ENTRADA_DUPLICADO =
            new EntryOrderError("008", "Este ID de produto já foi registrado nesta ordem de entrada. Por favor, insira um ID de produto único.");

    public static final RepassaUtilError PRODUTO_JA_EXISTENTE_EM_ORDEM_DE_ENTRADA =
            new EntryOrderError("009", "Este produto está associado a uma ordem de entrada em aberto. Verifique a ordem de entrada existente ou contate o suporte para assistência.");

    public static final RepassaUtilError ORDEM_DE_ENTRADA_NAO_ENCONTRADA =
            new EntryOrderError("010", "Não foi possível encontrar esta ordem de entrada.");

    public static final RepassaUtilError FOTOGRAFIA_NAO_FINALIZADA =
            new EntryOrderError("011", "O produto associado a este ID não possui fotografia finalizada. Entre em contato com o departamento responsável para atualizar as informações.");

    public static final RepassaUtilError ERRO_REMOVER_PRODUTO_OE =
            new EntryOrderError("012", "Erro! Ocorreu um erro ao tentar remover o produto da ordem de entrada. Por favor, tente novamente mais tarde.");

    public static final RepassaUtilError ITEM_NAO_ENCONTRADO =
            new EntryOrderError("013", "Item não encontrado ou já removido anteriormente.");

    public static final RepassaUtilError CODIGO_LACRE_INVALIDO =
            new EntryOrderError("014", "O código do lacre não pode conter caracteres especiais.");

    public static final RepassaUtilError CODIGO_LACRE_JA_EXISTE =
            new EntryOrderError("015", "O código do lacre já foi salvo.");

    public static final RepassaUtilError ORDEM_DE_ENTRADA_JA_POSSUI_LACRE =
            new EntryOrderError("016", "Lacre já existe");

    public static final RepassaUtilError LACRE_NAO_ECONTRADO =
            new EntryOrderError("017", "Não foi possível encontrar esse lacre.");

    public static final RepassaUtilError ALTERAR_MESMO_CODIGO_LACRE =
            new EntryOrderError("018", "O lacre já está cadastrado com esse mesmo código.");

    public static final RepassaUtilError DATA_INVALIDA =
            new EntryOrderError("019", "O formato da data não foi reconhecido.");

    public static final RepassaUtilError QUANTIDADE_INVALIDA =
            new EntryOrderError("020", "A quantidade de produtos não está valida.");

    public static final RepassaUtilError ERRO_REMOVER_PRODUTO_STATUS_DIFERENTE_DE_EM_PROGRESSO =
            new EntryOrderError("021", "Produto não pode ser removido, pois não está com status em progresso.");

    public static final RepassaUtilError ERRO_REMOVER_PRODUTO_EM_ORDEM_NAO_ABERTA =
            new EntryOrderError("022", "Produto não pode ser removido, pois a ordem de entrada não está aberta");

    public static final RepassaUtilError ERRO_INTERVALO_DATAS =
            new EntryOrderError("023", "O intervalo está incorreto, a segunda data não deve ser anterior a primeira.");

    public static final RepassaUtilError STATUS_ORDEM_ENTRADA_INVALIDO =
            new EntryOrderError("024", "O status da ordem de entrada é inválido.");

    public static final RepassaUtilError ERRO_AO_ATUALIZAR_STATUS_OE =
            new EntryOrderError("025", "Erro ao atualizar status da ordem de entrada.");

    public static final RepassaUtilError FINALIZACAO_NAO_PERMITIDA_OE_NAO_ESTA_ABERTA =
            new EntryOrderError("026", "A ordem de entrada não pode ser finalizada, pois está com status diferente de aberta.");

    public static final RepassaUtilError FINALIZACAO_NAO_PERMITIDA_OE_NAO_TEM_ITENS =
            new EntryOrderError("027", "A ordem de entrada não pode ser finalizada, pois não possui itens registrados.");

    public static final RepassaUtilError ERRO_AO_FINALIZAR_OE =
            new EntryOrderError("028", "Ocorreu um erro ao tentar finalizar a ordem de entrada. Por favor, tente novamente mais tarde.");

    public static final RepassaUtilError ORDEM_DE_ENTRADA_NAO_ABERTA =
            new EntryOrderError("029", "A ordem de entrada não está em aberto.");

    public static final RepassaUtilError ERRO_AO_REGISTRAR_PRODUTO =
            new EntryOrderError("030", "Ocorreu um erro ao registrar o produto na ordem de entrada.");

    public static final RepassaUtilError PRODUTO_NAO_ENVIADO_AO_HJ =
            new EntryOrderError("031", "Produto ainda não foi enviado ao Highjump.");

    public static final RepassaUtilError PRODUTO_STATUS_RENOVA_INVALIDO =
            new EntryOrderError("032", "Produto não pode ser adicionado à Ordem de Entrada, pois seu status não é válido.");

    public static final RepassaUtilError TIPO_ORDEM_ENTRADA_NAO_IMPLEMENTADO =
            new EntryOrderError("033", "Registro de produto não implementado para esse tipo de ordem de entrada.");

    public static final RepassaUtilError PRODUTO_NAO_E_NOVO =
            new EntryOrderError("034", "Essa ordem de entrada só permite o registro de produtos novos.");

    public static final RepassaUtilError PRODUTO_RENOVA_NOVO =
            new EntryOrderError("035", "Status Novo é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_CARRINHO =
            new EntryOrderError("036", "Status Incluído no carrinho é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_ATIVO =
            new EntryOrderError("037", "Status Ativo - Disponível para venda é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_PENDENTE =
            new EntryOrderError("038", "Status Pendente - Indisponível para compra é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_AGUARDANDO_PAGAMENTO =
            new EntryOrderError("039", "Status Aguardando pagamento é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_VENDIDO_ENTREGA =
            new EntryOrderError("040", "Status Vendido - Aguardando entrega é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_VENDIDO_DEVOLUCAO =
            new EntryOrderError("045", "Status Vendido - Aguardando devolução é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_VENDIDO_FINALIZACAO =
            new EntryOrderError("046", "Status Vendido - Aguardando finalização é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_DOADO =
            new EntryOrderError("047", "Status doado é inválido para registro do produto na Ordem de Reversa.");

    public static final RepassaUtilError PRODUTO_RENOVA_VENDIDO_FINALIZADO =
            new EntryOrderError("048", "Status Vendido - Finalizado é inválido para registro do produto na Ordem de Reversa.");

    public EntryOrderError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getErrorCode() {
        return APP_PREFIX.concat("_").concat(errorCode);
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

}
