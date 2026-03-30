package com.example.ticketbookingsystem.controller.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PageResponseAbstract {
    public int pageNumber;
    public int pageSize;
    public Long totalElements;
    public int totalPages;

}
