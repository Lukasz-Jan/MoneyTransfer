package com.lj.services;

import java.util.Objects;

public class CompositeId {
    String accountId;
    String currency;

    public CompositeId(String accountId, String currency) {
        this.accountId = accountId;
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompositeId that = (CompositeId) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, currency);
    }
}