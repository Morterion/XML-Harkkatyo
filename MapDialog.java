   // Kartankatseluohjelman graafinen k�ytt�liittym�
      

    import javax.swing.*;

    import java.awt.*;
    import java.awt.event.*;

    import java.io.IOException;
    import java.net.*;

    import java.util.ArrayList;
    import javax.xml.parsers.DocumentBuilder;
    import javax.xml.parsers.DocumentBuilderFactory;
    import javax.xml.parsers.ParserConfigurationException;

    import org.w3c.dom.Document;
    import org.w3c.dom.Element;
    import org.w3c.dom.Node;
    import org.w3c.dom.NodeList;
    import org.xml.sax.*;

   public class MapDialog extends JFrame {
     
      // K�ytt�liittym�n komponentit
     
      private JLabel imageLabel = new JLabel();
      private JPanel leftPanel = new JPanel();
     
      private JButton refreshB = new JButton("P�ivit�");
      private JButton leftB = new JButton("<");
      private JButton rightB = new JButton(">");
      private JButton upB = new JButton("^");
      private JButton downB = new JButton("v");
      private JButton zoomInB = new JButton("+");
      private JButton zoomOutB = new JButton("-");

      private final String BASE = "http://demo.mapserver.org/cgi-bin/wms?SERVICE=WMS&VERSION=1.1.1&REQUEST=";
      private final String MAP = "GetMap&BBOX=";
      private final String END = "&STYLES=&FORMAT=image/png&TRANSPARENT=true";
      private final int WIDTH = 953;
      private final int HEIGHT = 480;
      
      private double LATITUDE = -180;
      private double LONGITUDE = -90;
      private double ZOOM = 72;

      public MapDialog() throws Exception {
     
        // Valmistele ikkuna ja lis�� siihen komponentit
     
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
     
        add(imageLabel, BorderLayout.EAST);
     
        ButtonListener bl = new ButtonListener();
        refreshB.addActionListener(bl);  
        leftB.addActionListener(bl);
        rightB.addActionListener(bl);
        upB.addActionListener(bl);
        downB.addActionListener(bl);
        zoomInB.addActionListener(bl);
        zoomOutB.addActionListener(bl);
     
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        leftPanel.setMaximumSize(new Dimension(100, 600));

          populateLeftPanel();

        leftPanel.add(refreshB);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(leftB);
        leftPanel.add(rightB);
        leftPanel.add(upB);
        leftPanel.add(downB);
        leftPanel.add(zoomInB);
        leftPanel.add(zoomOutB);
     
        add(leftPanel, BorderLayout.WEST);
     
        imageLabel.setIcon(new ImageIcon(new URL(BASE + MAP + LATITUDE+","+LONGITUDE+","+(LATITUDE+(ZOOM*5)+","+(LONGITUDE+(ZOOM*2.5)+"&SRS=EPSG:4326&WIDTH=" + WIDTH + "&HEIGHT=" + HEIGHT + "&LAYERS=bluemarble,continents,country_bounds,cities" + END)))));
        pack();
        setVisible(true); 
     
      }
     
      public static void main(String[] args) throws Exception {
        new MapDialog();
      }

      private ArrayList<Node> getCapabilities() throws MalformedURLException, ParserConfigurationException, IOException, SAXException {
          ArrayList<Node> result = new ArrayList<>();
          URL capUrl = new URL(BASE + "GetCapabilities");
          DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
          DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
          Document doc = dBuilder.parse(capUrl.openStream());
          doc.normalizeDocument();
          NodeList plist = doc.getDocumentElement().getChildNodes();//doc.getElementById("Capability").getElementsByTagName("Layer");//[0].getElementsByTagName("Layers");
          for (int i = 0; i < plist.getLength(); i++) {
              Node node = plist.item(i);
              if (node.getNodeName().equals("Capability")) {
                  plist = plist.item(i).getChildNodes().item(9).getChildNodes();
                  for (int j = 0; j < plist.getLength(); j++) {
                      Node n = plist.item(j);
                      if (n.getNodeName().equals("Layer")) {
                          System.out.println("jee");
                          result.add(n);
                      }
                  }
                  break;
              }
          }
          return result;
      }

      private void populateLeftPanel() throws Exception{
          ArrayList<Node> properties = getCapabilities();
          properties.forEach(e ->{
              System.out.println(e.getNodeName());
              Element data = (Element) e;
              System.out.println(data.getElementsByTagName("Name").item(0).getTextContent()+"  "+data.getElementsByTagName("Title").item(0).getTextContent());
              leftPanel.add(new LayerCheckBox(data.getElementsByTagName("Name").item(0).getTextContent(),data.getElementsByTagName("Title").item(0).getTextContent(), true));
          });
      }

      // Kontrollinappien kuuntelija
      // KAIKKIEN NAPPIEN YHTEYDESS� VOINEE HY�DYNT�� updateImage()-METODIA
      private class ButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
          if(e.getSource() == refreshB) {
            try { updateImage(); } catch(Exception ex) { ex.printStackTrace(); }
          }
          if(e.getSource() == leftB) {
            // VASEMMALLE SIIRTYMINEN KARTALLA
            // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA P�IVIT� KUVA
              if((LATITUDE > -180) || (LATITUDE < 180)){             
              LATITUDE += 5;
              try { updateImage(); } catch(Exception ex) { ex.printStackTrace(); } 
              }
          }
          if(e.getSource() == rightB) {
            // OIKEALLE SIIRTYMINEN KARTALLA
            // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA P�IVIT� KUVA
              if((LATITUDE > -180) || (LATITUDE < 180)){
              LATITUDE -= 5;
              try { updateImage(); } catch(Exception ex) { ex.printStackTrace(); }          
              }
          }
          if(e.getSource() == upB) {
            // YL�SP�IN SIIRTYMINEN KARTALLA
            // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA P�IVIT� KUVA
              if((LONGITUDE > -90) || (LONGITUDE < 90)){             
              LONGITUDE -= 5; 
              try { updateImage(); } catch(Exception ex) { ex.printStackTrace(); }
              }
          }
          if(e.getSource() == downB) {
            // ALASP�IN SIIRTYMINEN KARTALLA
            // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA P�IVIT� KUVA
              if((LONGITUDE > -90) || (LONGITUDE < 90)){  
              LONGITUDE += 5;             
              try { updateImage(); } catch(Exception ex) { ex.printStackTrace(); }
              }
          }
          if(e.getSource() == zoomInB) {
            // ZOOM IN -TOIMINTO
            // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA P�IVIT� KUVA
              if(ZOOM != 1){
              ZOOM = ZOOM-5;
              try { 
              updateImage(); } catch(Exception ex) { ex.printStackTrace(); }
              }
          }
          if(e.getSource() == zoomOutB) {
            // ZOOM OUT -TOIMINTO
            // MUUTA KOORDINAATTEJA, HAE KARTTAKUVA PALVELIMELTA JA P�IVIT� KUVA
              if(ZOOM != 72){
              ZOOM = ZOOM+5;
              try {
              updateImage(); } catch(Exception ex) { ex.printStackTrace(); }
              }
          }
        }
      }
     
      // Valintalaatikko, joka muistaa karttakerroksen nimen
      private class LayerCheckBox extends JCheckBox {
        private String name = "";
        public LayerCheckBox(String name, String title, boolean selected) {
          super(title, null, selected);
          this.name = name;
        }
        public String getName() { return name; }
      }
     
      // Tarkastetaan mitk� karttakerrokset on valittu,
      // tehd��n uudesta karttakuvasta pyynt� palvelimelle ja p�ivitet��n kuva
      public void updateImage() throws Exception {
        String s = "";
     
        // Tutkitaan, mitk� valintalaatikot on valittu, ja
        // ker�t��n s:��n pilkulla erotettu lista valittujen kerrosten
        // nimist� (k�ytet��n haettaessa uutta kuvaa)
        Component[] components = leftPanel.getComponents();
        for(Component com:components) {
            if(com instanceof LayerCheckBox)
              if(((LayerCheckBox)com).isSelected()) s = s + com.getName() + ",";
        }
        if (s.endsWith(",")) s = s.substring(0, s.length() - 1);
     
        // getMap-KYSELYN URL-OSOITTEEN MUODOSTAMINEN JA KUVAN P�IVITYS ERILLISESS� S�IKEESS�
        // imageLabel.setIcon(new ImageIcon(new URL(BASE + MAP + "-180,-90,180,90&SRS=EPSG:4326&WIDTH=" + WIDTH + "&HEIGHT=" + HEIGHT + "&LAYERS=bluemarble" + END)));
          new MapUpdater(s).start();
      }
      
    private class MapUpdater extends Thread{
          public MapUpdater(String layers){
              super();
              this.layers = layers;
                
          }
          private String layers;

          public void run(){
              System.out.println("running");
              try {
                  imageLabel.setIcon(new ImageIcon(new URL(BASE + MAP + LATITUDE+","+LONGITUDE+","+(LATITUDE+(ZOOM*5)+","+(LONGITUDE+(ZOOM*2.5)+"&SRS=EPSG:4326&WIDTH=" + WIDTH + "&HEIGHT=" + HEIGHT + "&LAYERS="+ layers + END)))));              
                  }catch (Exception e){
                  e.printStackTrace();
              }
          }
    }


    } // MapDialog