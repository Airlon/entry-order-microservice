package br.com.repassa.repository;

import br.com.backoffice_repassa_utils_lib.error.exception.RepassaException;
import br.com.repassa.dto.EntryOrderListDTO;
import br.com.repassa.dto.FilterDTO;
import br.com.repassa.entity.EntryOrder;
import br.com.repassa.enums.EntryOrderProductStatusEnum;
import br.com.repassa.enums.SortingEnum;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.runtime.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

@ApplicationScoped
@Slf4j
public class EntryOrderRepository implements PanacheRepository<EntryOrder> {

    @Inject
    EntityManager entityManager;

    public long countByCode(String code) {
        return find("code", code).count();
    }

    public Optional<EntryOrder> findByCodeOptional(String code) {
        return find("code", code).firstResultOptional();
    }

    public List<EntryOrderListDTO> listEntryOrders(FilterDTO filterDTO) throws RepassaException {

        log.info("Montando o select para buscar a lista de EntryOrders");
        String sql = """
                    SELECT
                      TO_CHAR(eo.date_created_at, 'DD-MM-YYYY HH24:MI:SS') as registrationdate,
                      TO_CHAR(eo.date_updated_at, 'DD-MM-YYYY HH24:MI:SS') as updatedDate,
                      eo.type as type,
                      eo.code as code,
                      eo.user_created_id as registrationUserId,
                      eo.user_created_email as registrationUserEmail,
                      eo.user_updated_id as updatedUserId,
                      eo.user_updated_email as updatedUserEmail,
                      CAST((SELECT COUNT(1) FROM entry_order_product_log eop WHERE eop.entry_order_code = eo.code AND eop.entry_order_product_status = :sentEnum) AS INTEGER) AS sent,
                      CAST((SELECT COUNT(1) FROM entry_order_product_log eop WHERE eop.entry_order_code = eo.code AND eop.entry_order_product_status = :receivedEnum) AS INTEGER) as received,
                      '-/-' as countProducts,
                      eo.status as status,
                      eo.classification as classification
                    FROM public.entry_order eo
                    WHERE 1=1
                """;

        log.info("Criando os filtros");
        sql = this.sqlCreateFilter(filterDTO, sql);

        log.info("Adicionando ordenacao");
        sql = addSorting(filterDTO, sql);

        log.info("Aplicando os filtros nos parametros");
        Query nativeQuery = this.parameter(filterDTO, sql);
        setParameterIfNotNull(nativeQuery, "sentEnum", EntryOrderProductStatusEnum.SENT.getStatus(), String::toUpperCase);
        setParameterIfNotNull(nativeQuery, "receivedEnum", EntryOrderProductStatusEnum.RECEIVED.getStatus(), String::toUpperCase);

        log.info("Adicionando paginacao");
        nativeQuery.setFirstResult(filterDTO.getPage() * filterDTO.getSize());
        nativeQuery.setMaxResults(filterDTO.getSize());

        log.info("Busca sera realizada");
        List<EntryOrderListDTO> entryOrderList;
        try {
            entryOrderList = nativeQuery.unwrap(NativeQuery.class)
                    .setResultTransformer(Transformers.aliasToBean(EntryOrderListDTO.class))
                    .getResultList();
        } catch (NoResultException ex) {
            log.error("Falha ao buscar no banco, resultado retornara vazio.");
            LOGGER.info(ex.getMessage());
            return List.of();
        }
        log.info("Retornando os resultados");
        return entryOrderList;
    }

    public Integer listEntryOrdersQuantity(FilterDTO filterDTO) throws RepassaException {

        log.info("Montando o select para buscar a quantidade de EntryOrders");
        String sql = """
                    SELECT
                      COUNT(1) as quantity
                    FROM public.entry_order eo
                    WHERE 1=1
                """;

        log.info("Criando os filtros");
        sql = this.sqlCreateFilter(filterDTO, sql);

        log.info("Aplicando os filtros nos parametros");
        Query nativeQuery = this.parameter(filterDTO, sql);

        log.info("Busca sera realizada");
        try {
            Object result = nativeQuery.getSingleResult();
            long count = ((Number) result).longValue();
            log.info("Retornando o resultado");
            return Math.toIntExact(count);
        } catch (NoResultException ex) {
            log.error("Falha ao buscar no banco, resultado retornara vazio.");
            LOGGER.info(ex.getMessage());
        }
        return 0;
    }

    private String addSorting(FilterDTO filterDTO, String sql){
        if(Objects.nonNull(filterDTO.getSort())){
            if(filterDTO.getSort().getSorting().equals(SortingEnum.DATE_CREATE.getSorting())){
                sql = sql.concat(" order by TO_CHAR(eo.date_created_at, 'YYYYMMDDHH24:MI:SS') ");

            }else if(filterDTO.getSort().getSorting().equals(SortingEnum.DATE_UPDATE.getSorting())){
                sql = sql.concat(" order by TO_CHAR(eo.date_updated_at, 'YYYYMMDDHH24:MI:SS') ");
            }
            if(filterDTO.getSort().getDirection().equals(SortingEnum.DESC.getSorting()) && sql.contains("order by")){
                sql = sql.concat(" desc ");
            }else{
                sql = sql.concat(" asc ");
            }
        }else{
            sql = sql.concat(" order by TO_CHAR(eo.date_updated_at, 'YYYYMMDDHH24:MI:SS') desc ");
        }
       return sql;
    }

    private void addFilter(StringBuilder sql, String paramName, String condition) {
        if (Objects.nonNull(condition)) {
            log.info("Adicionando {} como filtro", paramName);
            sql.append(" and ").append(condition);
        }
    }

    private String sqlCreateFilter(FilterDTO filterDTO, String sqlInitial) {

        StringBuilder sql = new StringBuilder(sqlInitial);

        String dateUpdatedCondition = "";
        if(Objects.nonNull(filterDTO.getPrimaryUpdatedDate()) || Objects.nonNull(filterDTO.getSecundaryUpdatedDate())){
            if(!StringUtil.isNullOrEmpty(filterDTO.getPrimaryUpdatedDate())){
                if(StringUtil.isNullOrEmpty(filterDTO.getSecundaryUpdatedDate())){
                    dateUpdatedCondition = "CONCAT(TO_CHAR(eo.date_updated_at, 'DD-MM-YYYY'),' 00:00:00') = :datePrimary";
                }else{
                    dateUpdatedCondition = "eo.date_updated_at BETWEEN TO_TIMESTAMP(:datePrimary,'DD-MM-YYYY HH24:MI:SS') AND TO_TIMESTAMP(:dateSecundary,'DD-MM-YYYY HH24:MI:SS')";
                }
            }else if(!StringUtil.isNullOrEmpty(filterDTO.getSecundaryUpdatedDate())){
                dateUpdatedCondition = "eo.date_updated_at BETWEEN TO_TIMESTAMP('01-01-2023 00:00:00','DD-MM-YYYY HH24:MI:SS') AND TO_TIMESTAMP(:dateSecundary,'DD-MM-YYYY HH24:MI:SS')";
            }
        }

        addFilter(sql, "dateUpdated", !StringUtil.isNullOrEmpty(dateUpdatedCondition)
                ? dateUpdatedCondition
                : null);

        addFilter(sql, "type", Objects.nonNull(filterDTO.getType())
                ? "UPPER(eo.type) = :type"
                : null);

        addFilter(sql, "code", Objects.nonNull(filterDTO.getCode()) && filterDTO.getCode().length() >= 5
                ? "UPPER(eo.code) like :code"
                : null);

        addFilter(sql, "user", Objects.nonNull(filterDTO.getUser()) && filterDTO.getUser().length() >= 5
                ? "(UPPER(eo.user_created_id) like :user or UPPER(eo.user_updated_email) like :user)"
                : null);

        addFilter(sql, "countProducts", Objects.nonNull(filterDTO.getCountProducts())
                ? "(select count(0) from public.entry_order_product eop where eop.entry_order_id = eo.id) = :countProducts"
                : null);

        addFilter(sql, "status", Objects.nonNull(filterDTO.getStatus())
                ? "UPPER(eo.status) like :status"
                : null);

        addFilter(sql, "classification", Objects.nonNull(filterDTO.getClassification())
                ? "UPPER(eo.classification) = :classification"
                : null);

        return sql.toString();
    }

    private Query parameter(FilterDTO filterDTO,
                            String sql) throws RepassaException {

        Query nativeQuery = entityManager.createNativeQuery(sql);

        if(!StringUtil.isNullOrEmpty(filterDTO.getPrimaryUpdatedDate()))
            setParameterIfNotNull(nativeQuery, "datePrimary", filterDTO.getPrimaryUpdatedDate(), String::toUpperCase);
        if(!StringUtil.isNullOrEmpty(filterDTO.getSecundaryUpdatedDate()))
            setParameterIfNotNull(nativeQuery, "dateSecundary", filterDTO.getSecundaryUpdatedDate(), String::toUpperCase);
        if(Objects.nonNull(filterDTO.getCode()) && filterDTO.getCode().length() >= 3)
            setParameterIfNotNull(nativeQuery, "code", filterDTO.getCode(), s -> s.toUpperCase() + "%");
        setParameterIfNotNull(nativeQuery, "user", filterDTO.getUser(), s -> "%" + s.toUpperCase() + "%");
        setParameterIfNotNull(nativeQuery, "countProducts", filterDTO.getCountProducts(), Integer::parseInt);
        setParameterIfNotNull(nativeQuery, "status", filterDTO.getStatus(), s -> s.toUpperCase() + "%");
        setParameterIfNotNull(nativeQuery, "classification", filterDTO.getClassification(), String::toUpperCase);
        setParameterIfNotNull(nativeQuery, "type", filterDTO.getType(), String::toUpperCase);
        return nativeQuery;
    }

    private <T> void setParameterIfNotNull(Query nativeQuery, String paramName, T value, Function<T, ?> processor) {
        if (Objects.nonNull(value)) {
            nativeQuery.setParameter(paramName, processor.apply(value));
        }
    }
}
