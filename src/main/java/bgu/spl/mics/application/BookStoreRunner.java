package bgu.spl.mics.application;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(args[0]));
            Gson gson = new Gson();
            GsonObject gsonObject = gson.fromJson(bufferedReader, GsonObject.class);
            HashMap<Integer, Customer> customersHashMap = new HashMap<>();

            Inventory.getInstance().load(gsonObject.initialInventory);
            ResourcesHolder.getInstance().load(gsonObject.initialResources[0].vehicles);
            TimeService timeService = new TimeService(gsonObject.services.time.duration, gsonObject.services.time.speed);
            List<Thread> services = new LinkedList<>();  //list of all the thread except for time service thread

            //count down latch init
            int numOfServices = gsonObject.services.selling + gsonObject.services.inventoryService + gsonObject.services.logistics + gsonObject.services.resourcesService + gsonObject.services.customers.length;
            CountDownLatch countDownLatch = new CountDownLatch(numOfServices);

            //init the services
            SellingService[] sellingService = new SellingService[gsonObject.services.selling];
            for (int i = 0; i < gsonObject.services.selling; i++) {
                sellingService[i] = new SellingService(i, countDownLatch);
                services.add(new Thread(sellingService[i]));
            }
            InventoryService[] inventoryService = new InventoryService[gsonObject.services.inventoryService];
            for (int i = 0; i < gsonObject.services.inventoryService; i++) {
                inventoryService[i] = new InventoryService(i, countDownLatch);
                services.add(new Thread(inventoryService[i]));
            }
            LogisticsService[] logisticsService = new LogisticsService[gsonObject.services.logistics];
            for (int i = 0; i < gsonObject.services.logistics; i++) {
                logisticsService[i] = new LogisticsService(i, countDownLatch);
                services.add(new Thread(logisticsService[i]));
            }
            ResourceService[] resourceService = new ResourceService[gsonObject.services.resourcesService];
            for (int i = 0; i < gsonObject.services.resourcesService; i++) {
                resourceService[i] = new ResourceService(i, countDownLatch);
                services.add(new Thread(resourceService[i]));
            }
            Customer[] customers = new Customer[gsonObject.services.customers.length];
            for (int i = 0; i < gsonObject.services.customers.length; i++) {
                GsonObject.GsonCustomer customer = gsonObject.services.customers[i];
                customers[i] = new Customer(customer.name, customer.id, customer.address, customer.distance, customer.creditCard.amount, customer.creditCard.number);
                customersHashMap.put(customers[i].getId(), customers[i]);
            }
            APIService[] apiService = new APIService[customers.length];
            for (int i = 0; i < customers.length; i++) {
                LinkedList<BookOrderEvent> orders = new LinkedList<>();
                for (int j = 0; j < gsonObject.services.customers[i].orderSchedule.length; j++)
                    orders.add(new BookOrderEvent(customers[i], gsonObject.services.customers[i].orderSchedule[j].bookTitle, gsonObject.services.customers[i].orderSchedule[j].tick));
                apiService[i] = new APIService(i, orders, customers[i], countDownLatch);
                services.add(new Thread(apiService[i]));
            }
            //starting all threads except the time service thread
            for (Thread thread : services) {
                thread.start();
            }

            Thread timeThread = new Thread(timeService);
            countDownLatch.await(); //waits for all the services to start except time service
            timeThread.start(); //starting time service thread
            services.add(timeThread);

            for (Thread thread : services) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //prints
            printToFile(customersHashMap, args[1]);
            Inventory.getInstance().printInventoryToFile(args[2]);
            MoneyRegister.getInstance().printOrderReceipts(args[3]);
            printToFile(MoneyRegister.getInstance(), args[4]);
        }


        //object to parse the json input
        public class GsonObject {
            BookInventoryInfo[] initialInventory;
            GsonVehicles[] initialResources;
            public GsonServices services;

            class GsonVehicles {
                DeliveryVehicle[] vehicles;
            }

            class GsonServices {
                GsonTime time;
                int selling;
                int inventoryService;
                int logistics;
                int resourcesService;
                GsonCustomer[] customers;
            }

            class GsonTime {
                int speed;
                int duration;
            }

            class GsonCustomer {
                int id;
                String name;
                String address;
                int distance;
                GsonCreditCard creditCard;
                GsonOrderSchedule[] orderSchedule;
            }

            class GsonCreditCard {
                int number;
                int amount;
            }

            class GsonOrderSchedule {
                String bookTitle;
                int tick;
            }

        }

        //prints serializable object to file
        public static void printToFile (Object toPrint, String filename){
            try {
                FileOutputStream fos = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(toPrint);
                oos.close();
                fos.close();
            } catch (Exception e) {
            }
        }

        //count down the service initialization
        public static void countDown (CountDownLatch countDownLatch){
            synchronized (countDownLatch) {
                countDownLatch.countDown();
            }
        }
    }
