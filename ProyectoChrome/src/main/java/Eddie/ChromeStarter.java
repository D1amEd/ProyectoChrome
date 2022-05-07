package Eddie;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.ous.jtoml.ParseException;

public class ChromeStarter extends Thread {
    private static final String jsonDirection = "src/accounts.json";

    // private WebDriver driver;
    private WebDriver driver;
    private int id;
    private String twitchUser;
    private String twitchPassword;
    private CyclicBarrier barrera;
    private CyclicBarrier segundaBarrera;

    public ChromeStarter(JSONObject acc, int id, CyclicBarrier barrier, CyclicBarrier segundabarrera) {
        WebDriverManager.chromedriver().setup();
        ChromeDriver driver = new ChromeDriver();
        Dimension dm = new Dimension(800,600);
        driver.manage().window().setSize(dm);

        if(id==1)
        {driver.manage().window().setPosition(new Point(0,0));}
        else if(id==2)
        {driver.manage().window().setPosition(new Point(800,0));}
        else if(id==3)
        {driver.manage().window().setPosition(new Point(0,600));}
        else if(id==4)
        {driver.manage().window().setPosition(new Point(800,600));}

        
        this.driver = driver;
        this.twitchUser = (String) acc.get("username");
        this.twitchPassword = (String) acc.get("password");
        this.id = id;
        this.barrera = barrier;
        this.segundaBarrera= barrera;
    }

    public void run() {
        System.out.println("Running thread number: " + id);
        try {
            openTwitchAndLogIn(this.twitchUser, this.twitchPassword);
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private void openTwitchAndLogIn(String twitchUser, String twitchPassword) throws BrokenBarrierException {
        driver.get("https://www.twitch.tv/");
        // driver.findElement(By.xpath("//button[normalize-space()=\"Iniciar sesión\"]")).click(); // le da en iniciar
        // sesión
        
         driver.findElement(By.xpath("//button[normalize-space()=\"Log In\"]")).click();
         
        WebElement userElement = new WebDriverWait(driver, Duration.ofSeconds(300))
                .until(ExpectedConditions.elementToBeClickable(By.id("login-username")));
        userElement.sendKeys(twitchUser); // entra el usuario de twitch

        WebElement passElement = new WebDriverWait(driver, Duration.ofSeconds(300))
                .until(ExpectedConditions.elementToBeClickable(By.id("password-input")));
        passElement.sendKeys(twitchPassword); // entra la pass de twitch
        try {
            barrera.await();
        } catch (InterruptedException | BrokenBarrierException e1) {
            e1.printStackTrace();
        }
        driver.findElement(
                By.xpath("//html/body/div[3]/div/div/div/div/div/div[1]/div/div/div[3]/form/div/div[3]/button"))
                .click(); // le da ingresar

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        WebElement remindmelater = new WebDriverWait(driver, Duration.ofSeconds(300))
                .until(ExpectedConditions
                        .elementToBeClickable(
                                By.xpath("/html/body/div[3]/div/div/div/div/div/div/div/div[3]/div[2]/button")));
        remindmelater.click(); // le da click a recordar más tarde
        try {
            Thread.sleep(4000);
            driver.get("https://www.twitch.tv/brawlhalla"); // entra a brawlhalla
            segundaBarrera.await();
        } catch (InterruptedException |BrokenBarrierException e2) {
        }      Actions actionProvider = new Actions(driver);


        WebElement options = new WebDriverWait(driver, Duration.ofSeconds(30))
        .until(ExpectedConditions
            .elementToBeClickable(By.xpath(
                "/html/body/div[1]/div/div[2]/div[1]/main/div[2]/div[3]/div/div/div[2]/div/div[2]/div/div[2]/div/div/div[7]/div/div[2]/div[2]/div[1]/div[2]/div/button")));
    options.click();
    WebElement quality = new WebDriverWait(driver, Duration.ofSeconds(30))
        .until(ExpectedConditions
            .elementToBeClickable(By.xpath(
                "/html/body/div[1]/div/div[2]/div[1]/main/div[2]/div[3]/div/div/div[2]/div/div[2]/div/div/div/div/div[7]/div/div[2]/div[2]/div[1]/div[1]/div/div/div/div/div/div/div[3]/button")));
    quality.click();
    WebElement cienp = new WebDriverWait(driver, Duration.ofSeconds(30))
        .until(ExpectedConditions
            .elementToBeClickable(By.xpath(
                "/html/body/div[1]/div/div[2]/div[1]/main/div[2]/div[3]/div/div/div[2]/div/div[2]/div/div/div/div/div[7]/div/div[2]/div[2]/div[1]/div[1]/div/div/div/div/div/div/div[9]/div/div/div/label")));
    cienp.click();
    }

    public static void main(String[] args) {

        try {
            // Parse JSON File
            JSONObject json = readAccountJSON();    
            JSONArray sheet = (JSONArray) json.get("users");
            CyclicBarrier newBarrier = new CyclicBarrier(sheet.size());
            CyclicBarrier newSecondBarrier = new CyclicBarrier(sheet.size());
            System.out.println("Account JSON loaded correctly");
            // Create thread arraylist
            ArrayList<ChromeStarter> threadArray = new ArrayList<ChromeStarter>();
            int i = 1;
            for (Object o : sheet) {
                if (o instanceof JSONObject) {
                    ChromeStarter chrome = new ChromeStarter((JSONObject) o, i, newBarrier, newSecondBarrier);
                    chrome.start();
                    i++;
                }
            }
        } catch (Exception e) {
            System.err.println("Couldn't load account JSON " + e);
        }

    }

    private static JSONObject readAccountJSON()
            throws IOException, ParseException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(jsonDirection);
        Object obj = parser.parse(reader);
        JSONObject accountJSON = (JSONObject) obj;
        return accountJSON;
    }

}
