package rfid.csvhandler;

import java.util.Date;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

public class Bus{

    public Bus(){}

    public Bus(String BUS_STATION, String BUS_NUMBER_PLATE, String RFID_CODE, Date DATE_TIME, String MAC_ADDRESS){
        this.BUS_STATION = BUS_STATION;
        this.BUS_NUMBER_PLATE = BUS_NUMBER_PLATE;
        this.RFID_CODE = RFID_CODE;
        this.DATE_TIME = DATE_TIME;
        this.MAC_ADDRESS = MAC_ADDRESS;
    }

    @CsvBindByPosition(position = 0)
    private String BUS_STATION;

    @CsvBindByPosition(position = 1)
    private String BUS_NUMBER_PLATE;

    @CsvBindByPosition(position = 2)
    private String RFID_CODE;

    @CsvBindByPosition(position = 3)
    @CsvDate("dd.MM.yyyy")
    private Date DATE_TIME;

    @CsvBindByPosition(position = 7)
    private String MAC_ADDRESS;

    public String getBUS_STATION(){
        return this.BUS_STATION;
    }

    public String getBUS_NUMBER_PLATE(){
        return this.BUS_NUMBER_PLATE;
    }

    public String getRFID_CODE(){
        return this.RFID_CODE;
    }

    public Date getDATE_TIME(){
        return this.DATE_TIME;
    }

    public String getMAC_ADDRESS(){
        return this.MAC_ADDRESS;
    }

    public String toString(){
        return String.format("%s, %s, %s, %tN, %s", BUS_STATION, BUS_NUMBER_PLATE, RFID_CODE, DATE_TIME, MAC_ADDRESS);
    }

}