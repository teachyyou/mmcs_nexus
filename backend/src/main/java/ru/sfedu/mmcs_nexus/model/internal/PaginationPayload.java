package ru.sfedu.mmcs_nexus.model.internal;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.sfedu.mmcs_nexus.exceptions.WrongRequestParamException;
import ru.sfedu.mmcs_nexus.model.enums.controller.EntitySort;

public class PaginationPayload {
    private final Integer limit;
    private final Integer offset;
    private final String sort;
    private final String order;
    private final Sort dirSort;

    public PaginationPayload(Integer limit, Integer offset, String sort, String order, EntitySort entitySort) {
        validateSort(sort, order, entitySort);

        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
        this.order = order;

        if (sort == null) {
            this.dirSort = Sort.unsorted();
        } else {
            Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            this.dirSort = Sort.by(direction, sort);
        }
    }

    public PaginationPayload(Integer limit, Integer offset) {
        this.limit = limit;
        this.offset = offset;
        this.sort = null;
        this.order = null;

        this.dirSort = Sort.unsorted();
    }


    public Pageable getPageable() {
        //todo позже убрать необходимость кратности
        int page = offset / limit;

        return PageRequest.of(page, limit, dirSort);
    }

    private void validateSort(String sortParam, String order, EntitySort entitySort) {
        if (!(order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("desc"))) {
            throw new WrongRequestParamException(STR."Incorrect sorting order: \{order}");
        } else if (entitySort.isNotAllowed(sortParam)) {
            throw new WrongRequestParamException(STR."Incorrect sorting param: \{sortParam}");
        }
    }
}
