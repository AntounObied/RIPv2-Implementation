
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class dummy {


    public static void main(String[] args) throws UnknownHostException {
        String[] self = InetAddress.getLocalHost().toString().split("/");
        String ip = self[1];
        System.out.println(ip);
    }
}
