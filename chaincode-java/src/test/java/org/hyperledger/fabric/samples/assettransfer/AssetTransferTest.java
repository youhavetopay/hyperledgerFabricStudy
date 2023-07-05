/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class AssetTransferTest {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> assetList;
        private final Genson genson = new Genson();


        MockAssetResultsIterator() {
            super();

            assetList = new ArrayList<KeyValue>();

            Account sendUserAccount = new Account("asset1", "A", 2000L, AccountType.USER);
            Account receivingUserAccount = new Account("asset2", "B", 0L, AccountType.USER);
            Account commissionAccount = new Account("asset3", "creativehill", 0L, AccountType.COMMISSION);

            assetList.add(new MockKeyValue("asset1", genson.serialize(sendUserAccount)));
            assetList.add(new MockKeyValue("asset2", genson.serialize(receivingUserAccount)));
            assetList.add(new MockKeyValue("asset3", genson.serialize(commissionAccount)));

        }

        @Override
        public Iterator<KeyValue> iterator() {
            return assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

    }

    @Test
    public void invokeUnknownTransaction() {
        AssetTransfer contract = new AssetTransfer();
        Context ctx = mock(Context.class);

        Throwable thrown = catchThrowable(() -> {
            contract.unknownTransaction(ctx);
        });

        assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                .hasMessage("Undefined contract method called");
        assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo(null);

        verifyZeroInteractions(ctx);
    }

    @Nested
    class InvokeReadAssetTransaction {

        private final Genson genson = new Genson();

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);

            ChaincodeStub stub = mock(ChaincodeStub.class);
            Account expectingAccount = new Account("asset1", "blue", 5L, AccountType.USER);

            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1"))
                    .thenReturn("{ \"accountId\": \"asset1\", \"owner\": \"blue\", \"amount\": 5, \"type\": \"USER\" }");

            Account account = contract.ReadAsset(ctx, "asset1");

            assertThat(account).isEqualTo(expectingAccount);
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Throwable thrown = catchThrowable(() -> {
                contract.ReadAsset(ctx, "asset1");
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Account asset1 does not exist");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }
    }

    @Nested
    class InvokeCreateAssetTransaction {

        @Test
        public void whenAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1"))
                    .thenReturn("{ \"accountId\": \"asset1\", \"owner\": \"blue\", \"amount\": 5, \"type\": \"USER\" }");

            Throwable thrown = catchThrowable(() -> {
                contract.CreateAsset(ctx, "asset1", "blue", "45", AccountType.USER.name());
            });

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause()
                    .hasMessage("Account asset1 already exists");
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void whenAssetDoesNotExist() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn("");

            Account account = contract.CreateAsset(ctx, "asset1", "blue", "45", AccountType.USER.name());

            assertThat(account).isEqualTo(new Account("asset1", "blue", 45L, AccountType.USER));
        }
    }

    @Nested
    class QueryTransaction {

        private final Genson genson = new Genson();

        @Test
        public void whenAccountGetAll() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);

            Account sendUserAccount = new Account("asset1", "A", 2000L, AccountType.USER);
            Account receivingUserAccount = new Account("asset2", "B", 0L, AccountType.USER);
            Account commissionAccount = new Account("asset3", "creativehill", 0L, AccountType.COMMISSION);

            List<Account> accounts = new ArrayList<>();
            accounts.add(sendUserAccount);
            accounts.add(receivingUserAccount);
            accounts.add(commissionAccount);


            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());


            String result = contract.GetAllAssets(ctx);
            System.out.println(result);

            assertThat(result).isEqualTo(genson.serialize(accounts));
        }
    }

    @Nested
    class InvokeTransferAccountTransaction {

        private final Genson genson = new Genson();

        @Test
        public void whenTransferBetweenUsers() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);

            Account sendUserAccount = new Account("asset1", "A", 2000L, AccountType.USER);
            Account receivingUserAccount = new Account("asset2", "B", 0L, AccountType.USER);

            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn(genson.serialize(sendUserAccount));
            when(stub.getStringState("asset2")).thenReturn(genson.serialize(receivingUserAccount));
            when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());


            String result = contract.TransferAccount(ctx, "asset1", "asset2", "1000");

            Account expectedSendUserAccount = new Account("asset1", "A", 999L, AccountType.USER);
            Account expectedReceivingUserAccount = new Account("asset2", "B", 1000L, AccountType.USER);
            Account expectedCommissionAccount = new Account("asset3", "creativehill", 1L, AccountType.COMMISSION);

            assertThat(result).isEqualTo(expectedSendUserAccount + " -> " + expectedReceivingUserAccount + " => " + expectedCommissionAccount);
        }

        @Test
        public void whenTransferNotBetweenUsers() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);

            Account sendUserAccount = new Account("asset1", "A", 2000L, AccountType.COMPANY);
            Account receivingUserAccount = new Account("asset2", "B", 0L, AccountType.USER);

            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn(genson.serialize(sendUserAccount));
            when(stub.getStringState("asset2")).thenReturn(genson.serialize(receivingUserAccount));
//            when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());


            String result = contract.TransferAccount(ctx, "asset1", "asset2", "1000");

            Account expectedSendUserAccount = new Account("asset1", "A", 1000L, AccountType.COMPANY);
            Account expectedReceivingUserAccount = new Account("asset2", "B", 1000L, AccountType.USER);

            assertThat(result).isEqualTo(expectedSendUserAccount + " -> " + expectedReceivingUserAccount);
        }

        @Test
        public void whenTransferNotBetweenUsersAndBigAmount() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);

            Account sendUserAccount = new Account("asset1", "A", 800_000_000_000L, AccountType.COMPANY);
            Account receivingUserAccount = new Account("asset2", "B", 100_000_000_000L, AccountType.USER);

            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState("asset1")).thenReturn(genson.serialize(sendUserAccount));
            when(stub.getStringState("asset2")).thenReturn(genson.serialize(receivingUserAccount));
//            when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());


            String result = contract.TransferAccount(ctx, "asset1", "asset2", "1");

            Account expectedSendUserAccount = new Account("asset1", "A", 799_999_999_999L, AccountType.COMPANY);
            Account expectedReceivingUserAccount = new Account("asset2", "B", 100_000_000_001L, AccountType.USER);

            assertThat(result).isEqualTo(expectedSendUserAccount + " -> " + expectedReceivingUserAccount);
        }

    }
}
