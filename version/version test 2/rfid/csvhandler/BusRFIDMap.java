package rfid.csvhandler;

import com.opencsv.bean.CsvBindByName;

public class BusRFIDMap{
    
    @CsvBindByName(column="BUS_NUMBER_PLATE")
    String BUS_NUMBER_PLATE;

    @CsvBindByName(column="RFID_CODE")
    String RFID_CODE;

    public String getBUS_NUMBER_PLATE(){
        return this.BUS_NUMBER_PLATE;
    }

    public String getRFID_CODE(){
        return this.RFID_CODE;
    }
}
