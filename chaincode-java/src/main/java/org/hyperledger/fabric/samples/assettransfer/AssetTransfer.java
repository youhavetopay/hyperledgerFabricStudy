/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.assettransfer;

import java.util.ArrayList;
import java.util.List;


import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "Account Transfer",
                description = "The hyperlegendary Account transfer",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "a.transfer@example.com",
                        name = "Adrian Transfer",
                        url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements ContractInterface {

    private static Genson genson = new Genson();

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }


    /**
     * Creates a new Account on the ledger.
     *
     * @param ctx the transaction context
     * @param accountId the accountId of the new Account
     * @param owner the owner of the new Account
     * @param originAmount the originAmount for the new Account
     * @param originType the type for the new Account
     * @return the created Account
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Account CreateAsset(final Context ctx, final String accountId, final String owner, final String originAmount, final String originType) {
        ChaincodeStub stub = ctx.getStub();

        if (AssetExists(ctx, accountId)) {
            String errorMessage = String.format("Account %s already exists", accountId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Long amount = Long.parseLong(originAmount);
        AccountType type = AccountType.valueOf(originType);

        Account account = new Account(accountId, owner, amount, type);
        // Use Genson to convert the Asset into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(account);
        stub.putStringState(accountId, sortedJson);

        return account;
    }

    /**
     * Retrieves an asset with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param accountId the ID of the account
     * @return the asset found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Account ReadAsset(final Context ctx, final String accountId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(accountId);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Account %s does not exist", accountId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        Account account = genson.deserialize(assetJSON, Account.class);
        return account;
    }

    /**
     * Checks the existence of the asset on the ledger
     *
     * @param ctx the transaction context
     * @param accountId the ID of the account
     * @return boolean indicating the existence of the account
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean AssetExists(final Context ctx, final String accountId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(accountId);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    /**
     * Account transfer from ledger
     *
     * @param ctx the transaction context
     * @param sendingAccountId the sender's accountId
     * @param receivingAccountId the recipient's accountId
     * @param sendAmount the amount to send
     * @return the old owner
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public String TransferAccount(final Context ctx, final String sendingAccountId, final String receivingAccountId, final String sendAmount) {
        ChaincodeStub stub = ctx.getStub();
        String sendAccountJSON = stub.getStringState(sendingAccountId);
        String receivingAccountJSON = stub.getStringState(receivingAccountId);


        if (sendAccountJSON == null || sendAccountJSON.isEmpty()) {
            String errorMessage = String.format("Account %s does not exist", sendingAccountId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        if (receivingAccountJSON == null || receivingAccountJSON.isEmpty()) {
            String errorMessage = String.format("Account %s does not exist", receivingAccountId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }



        Account sendAccount = genson.deserialize(sendAccountJSON, Account.class);
        Account receivingAccount = genson.deserialize(receivingAccountJSON, Account.class);


        float commissionPercent = 0.0f;

        if (sendAccount.getType() == AccountType.USER && receivingAccount.getType() == AccountType.USER) {
            commissionPercent = 0.001f;
        }

        long convertSendAmount = Long.parseLong(sendAmount);
        long commission = (long) (convertSendAmount * commissionPercent);
        long updatedSendAccountOfAmount = sendAccount.getAmount() - (convertSendAmount + commission);
        long updatedReceivingAccountOfAmount = receivingAccount.getAmount() + convertSendAmount;


        Account updatedSendAccount = new Account(sendAccount.getAccountId(), sendAccount.getOwner(), updatedSendAccountOfAmount, sendAccount.getType());
        Account updatedReceivingAccount = new Account(receivingAccount.getAccountId(), receivingAccount.getOwner(), updatedReceivingAccountOfAmount, receivingAccount.getType());



        if (commission > 0) {
            Account commissionAccount = getCommissionAccount(ctx);

            if (commissionAccount == null) {
                String errorMessage = String.format("CommissionAccount does not exist");
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
            }

            long updatedCommissionAccountOfAmount = commissionAccount.getAmount() + (long) commission;
            Account updatedCommissionAccount = new Account(commissionAccount.getAccountId(), commissionAccount.getOwner(), updatedCommissionAccountOfAmount, commissionAccount.getType());

            stub.putStringState(updatedSendAccount.getAccountId(), genson.serialize(updatedSendAccount));
            stub.putStringState(updatedReceivingAccount.getAccountId(), genson.serialize(updatedReceivingAccount));
            stub.putStringState(updatedCommissionAccount.getAccountId(), genson.serialize(updatedCommissionAccount));

            return updatedSendAccount + " -> " + updatedReceivingAccount + " => " + updatedCommissionAccount;
        } else {
            stub.putStringState(updatedSendAccount.getAccountId(), genson.serialize(updatedSendAccount));
            stub.putStringState(updatedReceivingAccount.getAccountId(), genson.serialize(updatedReceivingAccount));

            return updatedSendAccount + " -> " + updatedReceivingAccount;
        }
    }

    /**
     * Retrieves all assets from the ledger.
     *
     * @param ctx the transaction context
     * @return array of assets found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllAssets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Account> queryResults = new ArrayList<Account>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Account account = genson.deserialize(result.getStringValue(), Account.class);
            queryResults.add(account);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    public static Account getCommissionAccount(final Context ctx) {

        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        if (results == null) {
            return null;
        }

        for (KeyValue result: results) {
            Account account = genson.deserialize(result.getStringValue(), Account.class);

            if (account.getType() == AccountType.COMMISSION) {
                return account;
            }
        }

        return null;
    }
}
