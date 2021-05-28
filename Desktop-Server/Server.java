public class Server {
    public static void main(String[] args)throws Exception {
        Communications c=new Communications();
        c.createHost();
        c.communicateAsHost();
    }
}
