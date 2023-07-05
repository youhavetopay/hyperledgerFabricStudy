/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class AssetTest {

    @Nested
    class Equality {

        @Test
        public void isReflexive() {
            Account account = new Account("asset1", "Blue", 100L, AccountType.USER);

            assertThat(account).isEqualTo(account);
        }

        @Test
        public void isSymmetric() {
            Account accountA = new Account("asset1", "Blue", 100L, AccountType.USER);
            Account accountB = new Account("asset2", "Green", 200L, AccountType.USER);

            assertThat(accountA).isEqualTo(accountA);
            assertThat(accountB).isEqualTo(accountB);
        }

        @Test
        public void isTransitive() {
            Account accountA = new Account("asset1", "Blue", 100L, AccountType.USER);
            Account accountB = new Account("asset2", "Green", 200L, AccountType.USER);
            Account accountC = new Account("asset3", "Red", 300L, AccountType.USER);

            assertThat(accountA).isEqualTo(accountA);
            assertThat(accountB).isEqualTo(accountB);
            assertThat(accountC).isEqualTo(accountC);
        }

        @Test
        public void handlesInequality() {
            Account accountA = new Account("asset1", "Blue", 100L, AccountType.USER);
            Account accountB = new Account("asset2", "Green", 200L, AccountType.USER);

            assertThat(accountA).isNotEqualTo(accountB);
        }

        @Test
        public void handlesOtherObjects() {
            Account accountA = new Account("asset1", "Blue", 100L, AccountType.USER);
            String assetB = "not a asset";

            assertThat(accountA).isNotEqualTo(assetB);
        }

        @Test
        public void handlesNull() {
            Account accountA = new Account("asset1", "Blue", 100L, AccountType.USER);

            assertThat(accountA).isNotEqualTo(null);
        }
    }

}
