package rfid.csvhandler;

import com.opencsv.bean.CsvBindByName;

public class BusRFIDMap{

    @CsvBindByName(column="RFID_CODE")
    private String RFID_CODE;

    public String getRFID_CODE(){
        return this.RFID_CODE;
    }
}
