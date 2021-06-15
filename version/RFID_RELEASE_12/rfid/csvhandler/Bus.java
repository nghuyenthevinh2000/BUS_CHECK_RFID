package rfid.csvhandler;

import com.opencsv.bean.CsvBindByPosition;

public class Bus{

    public Bus(){}

    public Bus(String RFID_CODE, String DATE_TIME, String MAC_ADDRESS){
        this.RFID_CODE = RFID_CODE;
        this.DATE_TIME = DATE_TIME;
        this.MAC_ADDRESS = MAC_ADDRESS;
    }

    @CsvBindByPosition(position = 1)
    private String RFID_CODE;

    @CsvBindByPosition(position = 2)
    private String DATE_TIME;

    @CsvBindByPosition(position = 3)
    private String MAC_ADDRESS;

    public String getRFID_CODE(){
        return this.RFID_CODE;
    }

    public String getDATE_TIME(){
        return this.DATE_TIME;
    }

    public String getMAC_ADDRESS(){
        return this.MAC_ADDRESS;
    }

    public String toString(){
        return String.format("%s, %s, %s", RFID_CODE, DATE_TIME, MAC_ADDRESS);
    }

}