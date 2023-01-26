package com.rco.rcotrucks.interfaces;

import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;

public interface CRUDInterface {
    public void onSaveCalled(TollReceiptModel tollReceiptModel);
    public void onSaveCalled(FuelReceiptModel tollReceiptModel);

    public void onDeleteCalled(ReceiptModel receiptModel);
    public void onDeleteCalled(FuelReceiptModel receiptModel);
}
