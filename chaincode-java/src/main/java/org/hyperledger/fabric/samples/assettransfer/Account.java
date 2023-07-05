/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;


@DataType()
public final class Account {

    @Property()
    private final String accountId;

    @Property()
    private final String owner;

    @Property()
    private final Long amount;

    @Property()
    private final AccountType type;

    public String getAccountId() {
        return accountId;
    }

    public String getOwner() {
        return owner;
    }

    public Long getAmount() {
        return amount;
    }

    public AccountType getType() {
        return type;
    }

    public Account(@JsonProperty("accountId") final String accountId, @JsonProperty("owner") final String owner, @JsonProperty("amount") final Long amount, @JsonProperty("type") final AccountType type
    ) {
        this.accountId = accountId;
        this.owner = owner;
        this.amount = amount;
        this.type = type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        Account other = (Account) obj;

        return Objects.deepEquals(
                new String[] {getAccountId(), getOwner(), String.valueOf(getAmount()), String.valueOf(getType())},
                new String[] {other.getAccountId(), other.getOwner(), String.valueOf(other.getAmount()), String.valueOf(other.getType())});
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountId(), getOwner(), getAmount(), getType());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [accountId=" + accountId + ", owner="
                + owner + ", amount=" + amount + ", type=" + type + "]";
    }
}
