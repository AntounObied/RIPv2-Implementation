/**
 * @author Antoun Obied
 *
 * This class represents an instance of a Pod, with the information used to broadcast
 */

public class Pod {

    private int addressFamily;
    private String address;
    private String nextHop;
    private String subnetMask;
    private int cost;
    private int id;

    public Pod(int id){
        this.id = id;
    }

    public int getID(){
        return id;
    }

    public int getAddressFamily(){
        return addressFamily;
    }

    public void setAddressFamily(int family){
        this.addressFamily = family;
    }

    public String getAddress(){ return address; }

    public void setAddress(String address){
        this.address = address;
    }

    public void setCost(int cost){
        this.cost = cost;
    }

    public int getCost(){
        return cost;
    }


    public void setNextHop(String nextHop){
        this.nextHop = nextHop;
    }

    public String getNextHop(){
        return nextHop.replace("/", "");
    }

    public String getSubnetMask(){
        return subnetMask;
    }


}
