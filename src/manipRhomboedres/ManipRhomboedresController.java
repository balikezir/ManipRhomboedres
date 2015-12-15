package manipRhomboedres;

import hvBase.LogFX;
import hvBase.Tempo;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import socket.SocketClientDigimetrie;
import socket.SocketClientMonochromateur;
import socket.SocketClientPuissanceMetre;
import socket.SocketClientSMC100;
import socket.SocketClientSimple;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ManipRhomboedresController implements Initializable {

	private SocketClientDigimetrie socketClientDigimetrie;
	private SocketClientPuissanceMetre socketClientPuissanceMetre;
	private SocketClientSMC100 socketClientSmc100;
	private SocketClientMonochromateur socketClientMonochromateur;

	//Liste des serveurs possibles
	private ArrayList<String> listeServeur = new ArrayList<String>();
	private ArrayList<Double> listeLambda = new ArrayList<Double>();
	private ArrayList<Double> listePositionsDeP1 = new ArrayList<Double>();

	private double positionMoteurP1;
	private double positionMoteurRhomb;
	private double positionMoteurTranslat;

	private int idMoteurP1 = 1;
	private int idMoteurRhomb = 2;
	private int idMoteurTranslatRhomb = 3;

	private int nbMesCell;
	private double fluxMoyCell1;
	private double incertCell1;
	private double fluxMoyCell2;
	private double incertCell2;
	private double darkMoyCell1;
	private double darkMoyCell2;
	private double incertDarkCell1;
	private double incertDarkCell2;

	private double posP1Extinction;
	private double posRhombExtinction;
	private double posP1;
	private int nbToursP1;
	private int numeroDuTourP1;
	private double incrementP1;
	private double posTranslatHorsFaisceau;
	private double posTranslatDansLeFaisceau;
	private double posTranslation;

	private double lambda;
	private int filtre;
	private double lambdaMin;
	private double lambdaMax;
	private double lambdaPas;

	private int pmNbMesures;
	private double pmMoy;
	private double pmIncert;

	private Tempo tempo;

	@FXML
	private TextField tfPortDigimetrie;
	@FXML
	private TextField tfNbMesCell;

	@FXML
	private TextField tfFluxMoyCell1;
	@FXML
	private TextField tfIncertCell1;
	@FXML
	private TextField tfFluxMoyCell2;
	@FXML
	private TextField tfIncertCell2;

	@FXML
	private TextField tfDarkMoyCell1;
	@FXML
	private TextField tfDarkMoyCell2;

	@FXML
	private TextField tfIncertDarkCell1;
	@FXML
	private TextField tfIncertDarkCell2;

	@FXML
	private TextField tfPortSmc100;
	@FXML
	private TextField tfPosP1Extinction;
	@FXML
	private TextField tfPosRhombExtinction;
	@FXML
	private TextField tfPosP1;
	@FXML
	private TextField tfNbToursP1;
	@FXML
	private TextField tfNumeroDuTourP1;
	@FXML
	private TextField tfIncrementP1;
	@FXML
	private TextField tfPosTranslatHorsFaisceau;
	@FXML
	private TextField tfPosTranslatDansFaisceau;
	@FXML
	private TextField tfPosTranslation;
	@FXML
	private TextField tfPosRhomb;

	@FXML
	private TextField tfPortMonochromateur;
	@FXML
	private TextField tfLambda;
	@FXML
	private TextField tfFiltre;
	@FXML
	private TextField tfLambdaMin;
	@FXML
	private TextField tfLambdaMax;
	@FXML
	private TextField tfLambdaPas;

	@FXML
	private TextField tfPortPuissanceMetre;
	@FXML
	private TextField tfPMNbMesures;
	@FXML
	private TextField tfPMMoy;
	@FXML
	private TextField tfPMIncert;

	@FXML
	private ComboBox<String> comboBoxServeurDigimetrie;
	@FXML
	private ComboBox<String> comboBoxServeurSmc100;
	@FXML
	private ComboBox<String> comboBoxServeurMonochromateur;
	@FXML
	private ComboBox<String> comboBoxServeurPuissanceMetre;

	@FXML
	private TextField tfRefRhomb;
	@FXML
	private TextField tfInfos;

	@FXML
	private TextArea taLog;
	private LogFX taLog2;
	@FXML
	private Button boutonValiderConfig;
	@FXML
	private Button boutonDemarrage;
	@FXML
	private Button boutonEnregistrer;

	private Service<Void> sequencementManipThread;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		//Définition de la liste des serveurs possibles
		//Local
		listeServeur.add("localhost");
		//PC Manip
		listeServeur.add("10.123.1.57");
		//Pourrait indiquer si on ne rend pas actif ce serveur ou si on n'utilise pas cet appareil
		listeServeur.add("INACTIF");
		//Pour idiquer que l'appareil est simulé
		listeServeur.add("SIMULATION");

		//Charge les combobox et mets la valeur par défaut
		chargeComboBoxString(comboBoxServeurDigimetrie, listeServeur, 0);
		chargeComboBoxString(comboBoxServeurSmc100, listeServeur, 0);
		chargeComboBoxString(comboBoxServeurMonochromateur, listeServeur, 0);
		chargeComboBoxString(comboBoxServeurPuissanceMetre, listeServeur, 0);

		//Initialise TaLog2
		taLog2 = new LogFX();
		taLog2.setTextArea(taLog);

		tempo = new Tempo();
	}

	//Action sur le bouton
	@FXML
	public void validerConfiguration(ActionEvent event) {
		//
		changeAdresseServeurDigimetrie();
		changeAdresseServeurMonochromateur();
		changeAdresseServeurPuissanceMetre();
		changeAdresseServeurSmc100();

		//Charge les valeurs des adresses IP
		String chaineServeurDigimetrie = comboBoxServeurDigimetrie.getValue();
		String chaineServeurMonochromateur = comboBoxServeurMonochromateur.getValue();
		String chaineServeurPuissanceMetre = comboBoxServeurPuissanceMetre.getValue();
		String chaineServeurSmc100 = comboBoxServeurSmc100.getValue();

		//Charge les valeurs des ports
		int portServeurDigimetrie = Integer.valueOf(tfPortDigimetrie.getText());
		int portServeurPuissanceMetre = Integer.valueOf(tfPortPuissanceMetre.getText());
		int portServeurSmc100 = Integer.valueOf(tfPortSmc100.getText());
		int portServeurMonochromateur = Integer.valueOf(tfPortMonochromateur.getText());

		//Charge les valeurs des paramètres
		setNbMesCell(Integer.valueOf(tfNbMesCell.getText()));
		setPosP1Extinction(Double.valueOf(tfPosP1Extinction.getText()));
		setPosRhombExtinction(Double.valueOf(tfPosRhombExtinction.getText()));
		setPmNbMesures(Integer.valueOf(tfPMNbMesures.getText()));
		setLambdaMin(Double.valueOf(tfLambdaMin.getText()));
		setLambdaMax(Double.valueOf(tfLambdaMax.getText()));
		setLambdaPas(Double.valueOf(tfLambdaPas.getText()));
		setNbToursP1(Integer.valueOf(tfNbToursP1.getText()));
		setIncrementP1(Double.valueOf(tfIncrementP1.getText()));
		setPosTranslatDansLeFaisceau(Double.valueOf(tfPosTranslatDansFaisceau.getText()));
		setPosTranslatHorsFaisceau(Double.valueOf(tfPosTranslatHorsFaisceau.getText()));

		//Charge les Lambdas dans la liste
		for(double incLambdas = getLambdaMin(); incLambdas <= getLambdaMax(); incLambdas = incLambdas + getLambdaPas()){
			listeLambda.add(incLambdas);
		}

		//Charge les Positions pour P1 dans la liste
		for(double incPositionsP1Dans1Tour = 0; incPositionsP1Dans1Tour <= 360; incPositionsP1Dans1Tour = incPositionsP1Dans1Tour + getIncrementP1()){
			listePositionsDeP1.add(incPositionsP1Dans1Tour);
		}

		//Instancie les Clients sockets
		System.out.println("Ouverture des clients");

		socketClientDigimetrie = new SocketClientDigimetrie("localhost", chaineServeurDigimetrie, portServeurDigimetrie);
		System.out.println("Ouverture du client Digimetrie : IP = " + socketClientDigimetrie.getNomAdresseServeur() + " port : " + socketClientDigimetrie.getPort());

		socketClientPuissanceMetre = new SocketClientPuissanceMetre("localhost", chaineServeurPuissanceMetre, portServeurPuissanceMetre);
		System.out.println("Ouverture du client PuissanceMètre : IP = " + socketClientPuissanceMetre.getNomAdresseServeur() + " port : " + socketClientPuissanceMetre.getPort());

		socketClientSmc100 = new SocketClientSMC100("localhost", chaineServeurSmc100, portServeurSmc100);
		System.out.println("Ouverture du client Smc100 : IP = " + socketClientSmc100.getNomAdresseServeur() + " port : " + socketClientSmc100.getPort());

		socketClientMonochromateur = new SocketClientMonochromateur("localhost", chaineServeurMonochromateur, portServeurMonochromateur);
		System.out.println("Ouverture du client Monchromateur : IP = " + socketClientMonochromateur.getNomAdresseServeur() + " port : " + socketClientMonochromateur.getPort());

		boutonValiderConfig.setDisable(true);
		boutonDemarrage.setDisable(false);
		boutonEnregistrer.setDisable(true);
	}

	@FXML
	public void demarrage(ActionEvent event) {

		sequencementManipThread = new Service<Void>() {
			@Override
			protected Task<Void> createTask() {
				// TODO Auto-generated method stub
				return new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						// TODO Auto-generated method stub

						System.out.println("Démarrage de la manip");
						taLog2.ecrireLigneLog("Démarrage de la manip");

//						long dateDebut = System.currentTimeMillis();
//						long dateCourante = System.currentTimeMillis();

						System.out.println("Déplace P1 pour obtenir l'extinction");
						taLog2.ecrireLigneLog("Déplace P1 pour obtenir l'extinction");
						//Initialisation de positions des platines
						//Place P1 sur la position d'extinction avec le Wollaston
						deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurP1(), posP1Extinction);

						System.out.println("Déplace Le Rhomboèdre dans le faisceau");
						taLog2.ecrireLigneLog("Déplace Le Rhomboèdre dans le faisceau");
						//Place le Rhomboèdre dans le faisceau
						deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurTranslatRhomb(), posTranslatDansLeFaisceau);

						System.out.println("Déplace Le Rhomboèdre pour obtenir l'extinction");
						taLog2.ecrireLigneLog("Déplace Le Rhomboèdre pour obtenir l'extinction");
						//Place le rhomboèdre sur la position d'extinction avec le Wollaston
						deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurRhomb(), getPosRhombExtinction());

						System.out.println("Déplace Le Rhomboèdre de 45°");
						taLog2.ecrireLigneLog("Déplace Le Rhomboèdre de 45°");
						//Place le rhomboèdre sur la position 45 ° de l'extinction avec le Wollaston
						deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurRhomb(), (getPositionMoteurRhomb() + 45));

						//Pour chaque lambda
						for(int incLambdas = 0; incLambdas < getListeLambda().size(); incLambdas ++){
							lambda = getListeLambda().get(incLambdas);

							System.out.println("Lambda = " + lambda);
							taLog2.ecrireLigneLog("Lambda = " + lambda);
							tfLambda.setText(String.valueOf(lambda));

							//Sélectionne la longueur d'onde sur le monochromateur
							socketClientMonochromateur.setEcritChangeLambda(lambda, taLog2);

							//Sélectionne le filtre du monochromateur
							//ATTENTION : Doit choisir le filtre en fonction de Lambda
							setFiltre(1);
							socketClientMonochromateur.setEcritChangeFiltre(getFiltre(), taLog2);
							tfFiltre.setText(String.valueOf(getFiltre()));

							//Sélectionne la longueur d'onde sur le puissance-mètre
							socketClientPuissanceMetre.setEcritChangeLambda(lambda, taLog2);

							//Pour Chaque positions de P1
							//Pour chaque tour
							for(int incNbTours = 1; incNbTours <= getNbToursP1(); incNbTours++){
								System.out.println("Tour N° : " + incNbTours);
								taLog2.ecrireLigneLog("Tour N° : " + incNbTours);
								tfNumeroDuTourP1.setText(String.valueOf(incNbTours));

								//Pour chaque position de P1 dans 1 tour
								for (int incPosP1 = 0; incPosP1 < getListePositionsDeP1().size(); incPosP1++) {
									System.out.println("Position de P1 demandée : " + getListePositionsDeP1().get(incPosP1));
									taLog2.ecrireLigneLog("Position de P1 demandée : " + getListePositionsDeP1().get(incPosP1));
									//Déplace le Moteur P1
									deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurP1(), getListePositionsDeP1().get(incPosP1));


									//************************Mesure du Flux Moyen !!!!!!!!!!!!!!!!!!
									//************************Mesure du Flux Moyen !!!!!!!!!!!!!!!!!!
									//************************Mesure du Flux Moyen !!!!!!!!!!!!!!!!!!
									//Ouvre l'obturateur
									ouvreObturateur();
									tempo.tempo(4000);
									//Lit la valeur du flux Cell1 et Cell2
									//Faire une fonction qui lit les valeurs et les trie pour avoir les valeurs des moyennes et écarts types etc...
									//Et qui note les valeurs du signel
									socketClientDigimetrie.setEcritLireTensionMultipleMoy(2, getNbMesCell(), taLog2);
									tempo.tempo(4000);

									setFluxMoyCell1(socketClientDigimetrie.getValeurTensionDeLaListe(0));
									setFluxMoyCell2(socketClientDigimetrie.getValeurTensionDeLaListe(1));
									setIncertCell1(socketClientDigimetrie.getValeurEcartTypeDeLaListe(0));
									setIncertCell2(socketClientDigimetrie.getValeurEcartTypeDeLaListe(1));

									//Ecrit les valeurs moyennes et écarts types pour les Flux
									tfFluxMoyCell1.setText(String.valueOf(getFluxMoyCell1()));
									tfFluxMoyCell2.setText(String.valueOf(getFluxMoyCell2()));
									tfIncertCell1.setText(String.valueOf(getIncertCell1()));
									tfIncertCell2.setText(String.valueOf(getIncertCell2()));

									//************************Fin du Flux Moyen !!!!!!!!!!!!!!!!!!
									//************************Fin du Flux Moyen !!!!!!!!!!!!!!!!!!
									//************************Fin du Flux Moyen !!!!!!!!!!!!!!!!!!

									//Lit la valeur du flux de la source
									socketClientPuissanceMetre.setEcritLitFlux(getPmNbMesures(), taLog2);
									tempo.tempo(4000);

									setPmMoy(socketClientPuissanceMetre.getMoyenneFluxArrondi(5));
									setPmIncert(socketClientPuissanceMetre.getIncertFluxArrondi(5));

									tfPMMoy.setText(String.valueOf(getPmMoy()));
									tfPMIncert.setText(String.valueOf(getPmIncert()));

									//************************DARK !!!!!!!!!!!!!!!!!!
									//************************DARK !!!!!!!!!!!!!!!!!!
									//************************DARK !!!!!!!!!!!!!!!!!!
									//Ferme l'obturateur
									fermeObturateur();
									tempo.tempo(4000);
									//Lit la valeur du flux Cell1 et Cell2
									//Faire une fonction qui lit les valeurs et les trie pour avoir les valeurs des moyennes et écarts types etc...
									//Et qui note les valeurs du Dark
									socketClientDigimetrie.setEcritLireTensionMultipleMoy(2, getNbMesCell(), taLog2);
									tempo.tempo(4000);

									//Ecrit les valeurs moyennes et écarts types pour les Darks
									setDarkMoyCell1(socketClientDigimetrie.getValeurTensionDeLaListe(0));
									setDarkMoyCell2(socketClientDigimetrie.getValeurTensionDeLaListe(1));
									setIncertDarkCell1(socketClientDigimetrie.getValeurEcartTypeDeLaListe(0));
									setIncertDarkCell2(socketClientDigimetrie.getValeurEcartTypeDeLaListe(1));

									//Ecrit les valeurs moyennes et écarts types pour les Flux
									tfDarkMoyCell1.setText(String.valueOf(getDarkMoyCell1()));
									tfDarkMoyCell2.setText(String.valueOf(getDarkMoyCell2()));
									tfIncertDarkCell1.setText(String.valueOf(getIncertDarkCell1()));
									tfIncertDarkCell2.setText(String.valueOf(getIncertDarkCell2()));

									//************************FIN DES DARKS !!!!!!!!!!!!!!!!!!
									//************************FIN DES DARKS !!!!!!!!!!!!!!!!!!
									//************************FIN DES DARKS !!!!!!!!!!!!!!!!!!

									//Charger la série de mesures spécifique


									//					long dateDebut = System.currentTimeMillis();
									//					long dateCourante = System.currentTimeMillis();

									//Charge le log
									taLog2.ecrireLigneLog("Ouvre l'obturateur");
									ouvreObturateur();
									tempo.tempo(4000);
								}
								//Tourne P1 pour la position demandée

								//Si P1 demandé = 0 et que incNbTours > 1 Va à -10° pour rattrapper le jeu

							}
						}

						//Retour des platines à 0
						deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurP1(), 0);
						deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurRhomb(), 0);
						deplaceAbsoluEtAffichePositionDeLaPlatine(getIdMoteurTranslatRhomb(), 0);

						//Ouvre obturateur
						ouvreObturateur();
						tempo.tempo(4000);

						System.out.println("Finalisation du test");
						taLog2.ecrireLigneLog("Finalisation du test");

						//Fermeture des sockets
						socketClientDigimetrie.fermer();
						socketClientPuissanceMetre.fermer();
						socketClientSmc100.fermer();
						socketClientMonochromateur.fermer();

						System.out.println("Fin de la manip, serveurs fermés");
						taLog2.ecrireLigneLog("Fin de la manip, serveurs fermés");

						boutonValiderConfig.setDisable(true);
						boutonDemarrage.setDisable(true);
						boutonEnregistrer.setDisable(false);

						//Retourne
						return null;
					}
				};
			}
		};
		//Cas où tout s'est bien passé
		sequencementManipThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Auto-generated method stub
				System.out.println("Ca fonctionne !!!");
			}
		});
		sequencementManipThread.restart();
	}

	@FXML
	public void enregistrer(ActionEvent event) {
		System.out.println("Enregistrement du fichier Resultat");
		taLog2.ecrireLigneLog("Enregistrement du fichier Resultat");

		boutonValiderConfig.setDisable(true);
		boutonDemarrage.setDisable(true);
		boutonEnregistrer.setDisable(true);
	}

	@FXML
	public void connexionDigimetrie(ActionEvent event) {
		System.out.println("Connexion Digimetrie");
		taLog2.ecrireLigneLog("Connexion Digimetrie");
	}

	@FXML
	public void connexionSmc100(ActionEvent event) {
		System.out.println("Connexion Smc100");
		taLog2.ecrireLigneLog("Connexion Smc100");
	}

	@FXML
	public void connexionMonochromateur(ActionEvent event) {
		System.out.println("Connexion Monochromateur");
		taLog2.ecrireLigneLog("Connexion Monochromateur");
	}

	@FXML
	public void connexionPuissanceMetre(ActionEvent event) {
		System.out.println("Connexion Puissance Mètre");
		taLog2.ecrireLigneLog("Connexion Puissance Mètre");
	}

	/**
	 * @return the socketClientManip
	 */
	public SocketClientSimple getSocketClient1() {
		return socketClientDigimetrie;
	}

	/**
	 * @param socketClientDigimetrie the socketClientManip to set
	 */
	public void setSocketClientManip1(SocketClientDigimetrie socketClient1) {
		this.socketClientDigimetrie = socketClient1;
	}

	/**
	 * @return the socketClientManip2
	 */
	public SocketClientSimple getSocketClient2() {
		return socketClientPuissanceMetre;
	}

	/**
	 * @param socketClientPuissanceMetre the socketClientManip2 to set
	 */
	public void setSocketClientManip2(SocketClientPuissanceMetre socketClient2) {
		this.socketClientPuissanceMetre = socketClient2;
	}

	/**
	 * @return the socketClientManip3
	 */
	public SocketClientSimple getSocketClient3() {
		return socketClientSmc100;
	}

	/**
	 * @param socketClientSmc100 the socketClientManip3 to set
	 */
	public void setSocketClientManip3(SocketClientSMC100 socketClient3) {
		this.socketClientSmc100 = socketClient3;
	}

	/**
	 * @return the socketClientManip4
	 */
	public SocketClientSimple getSocketClientManip4() {
		return socketClientMonochromateur;
	}

	/**
	 * @param socketClientManip4 the socketClientManip4 to set
	 */
	public void setSocketClientManip4(SocketClientMonochromateur socketClientManip4) {
		this.socketClientMonochromateur = socketClientManip4;
	}

	/**
	 *	Charge la comboBox à l'aide d'une liste de chaîne et indique le numéro de la ligne sélectionnée
	 */
	public void chargeComboBoxString(ComboBox<String> comboBox, ArrayList<String> liste, int numeroDeSelection) {
		for (int i = 0; i < liste.size(); i++) {
			comboBox.getItems().add(liste.get(i));
		}
		comboBox.getSelectionModel().select(numeroDeSelection);
	}

	/**
	 *
	 */
	public void changeAdresseServeurDigimetrie() {
		taLog2.ecrireLigneLog("Adresse serveur Digimétrie changée : " + comboBoxServeurDigimetrie.getSelectionModel().getSelectedItem());
	}

	/**
	 *
	 */
	public void changeAdresseServeurMonochromateur() {
		taLog2.ecrireLigneLog("Adresse serveur Monochromateur changée : " + comboBoxServeurMonochromateur.getSelectionModel().getSelectedItem());
	}

	/**
	 *
	 */
	public void changeAdresseServeurSmc100() {
		taLog2.ecrireLigneLog("Adresse serveur Smc100 changée : " + comboBoxServeurSmc100.getSelectionModel().getSelectedItem());
	}

	/**
	 *
	 */
	public void changeAdresseServeurPuissanceMetre() {
		taLog2.ecrireLigneLog("Adresse serveur Puissance Mètre changée : " + comboBoxServeurPuissanceMetre.getSelectionModel().getSelectedItem());
	}

	/**
	 * @return the listeLambda
	 */
	public ArrayList<Double> getListeLambda() {
		return listeLambda;
	}

	/**
	 * @param listeLambda the listeLambda to set
	 */
	public void setListeLambda(ArrayList<Double> listeLambda) {
		this.listeLambda = listeLambda;
	}

	/**
	 * @return the listePositionsDeP1
	 */
	public ArrayList<Double> getListePositionsDeP1() {
		return listePositionsDeP1;
	}

	/**
	 * @param listePositionsDeP1 the listePositionsDeP1 to set
	 */
	public void setListePositionsDeP1(ArrayList<Double> listePositionsDeP1) {
		this.listePositionsDeP1 = listePositionsDeP1;
	}

	/**
	 * @return the nbMesCell
	 */
	public int getNbMesCell() {
		return nbMesCell;
	}

	/**
	 * @param nbMesCell the nbMesCell to set
	 */
	public void setNbMesCell(int nbMesCell) {
		this.nbMesCell = nbMesCell;
	}

	/**
	 * @return the fluxMoyCell1
	 */
	public double getFluxMoyCell1() {
		return fluxMoyCell1;
	}

	/**
	 * @param fluxMoyCell1 the fluxMoyCell1 to set
	 */
	public void setFluxMoyCell1(double fluxMoyCell1) {
		this.fluxMoyCell1 = fluxMoyCell1;
	}

	/**
	 * @return the incertCell1
	 */
	public double getIncertCell1() {
		return incertCell1;
	}

	/**
	 * @param incertCell1 the incertCell1 to set
	 */
	public void setIncertCell1(double incertCell1) {
		this.incertCell1 = incertCell1;
	}

	/**
	 * @return the fluxMoyCell2
	 */
	public double getFluxMoyCell2() {
		return fluxMoyCell2;
	}

	/**
	 * @param fluxMoyCell2 the fluxMoyCell2 to set
	 */
	public void setFluxMoyCell2(double fluxMoyCell2) {
		this.fluxMoyCell2 = fluxMoyCell2;
	}

	/**
	 * @return the incertCell2
	 */
	public double getIncertCell2() {
		return incertCell2;
	}

	/**
	 * @param incertCell2 the incertCell2 to set
	 */
	public void setIncertCell2(double incertCell2) {
		this.incertCell2 = incertCell2;
	}

	/**
	 * @return the positionMoteurP1
	 */
	public double getPositionMoteurP1() {
		return positionMoteurP1;
	}

	/**
	 * @param positionMoteurP1 the positionMoteurP1 to set
	 */
	public void setPositionMoteurP1(double positionMoteurP1) {
		this.positionMoteurP1 = positionMoteurP1;
	}

	/**
	 * @return the positionMoteurRhomb
	 */
	public double getPositionMoteurRhomb() {
		return positionMoteurRhomb;
	}

	/**
	 * @param positionMoteurRhomb the positionMoteurRhomb to set
	 */
	public void setPositionMoteurRhomb(double positionMoteurRhomb) {
		this.positionMoteurRhomb = positionMoteurRhomb;
	}

	/**
	 * @return the positionMoteurTranslat
	 */
	public double getPositionMoteurTranslat() {
		return positionMoteurTranslat;
	}

	/**
	 * @param positionMoteurTranslat the positionMoteurTranslat to set
	 */
	public void setPositionMoteurTranslat(double positionMoteurTranslat) {
		this.positionMoteurTranslat = positionMoteurTranslat;
	}

	/**
	 * @return the idMoteurP1
	 */
	public int getIdMoteurP1() {
		return idMoteurP1;
	}

	/**
	 * @param idMoteurP1 the idMoteurP1 to set
	 */
	public void setIdMoteurP1(int idMoteurP1) {
		this.idMoteurP1 = idMoteurP1;
	}

	/**
	 * @return the idMoteurRhomb
	 */
	public int getIdMoteurRhomb() {
		return idMoteurRhomb;
	}

	/**
	 * @param idMoteurRhomb the idMoteurRhomb to set
	 */
	public void setIdMoteurRhomb(int idMoteurRhomb) {
		this.idMoteurRhomb = idMoteurRhomb;
	}

	/**
	 * @return the idMoteurTranslatRhomb
	 */
	public int getIdMoteurTranslatRhomb() {
		return idMoteurTranslatRhomb;
	}

	/**
	 * @param idMoteurTranslatRhomb the idMoteurTranslatRhomb to set
	 */
	public void setIdMoteurTranslatRhomb(int idMoteurTranslatRhomb) {
		this.idMoteurTranslatRhomb = idMoteurTranslatRhomb;
	}

	/**
	 * @return the darkMoyCell1
	 */
	public double getDarkMoyCell1() {
		return darkMoyCell1;
	}

	/**
	 * @param darkMoyCell1 the darkMoyCell1 to set
	 */
	public void setDarkMoyCell1(double darkMoyCell1) {
		this.darkMoyCell1 = darkMoyCell1;
	}

	/**
	 * @return the darkMoyCell2
	 */
	public double getDarkMoyCell2() {
		return darkMoyCell2;
	}

	/**
	 * @param darkMoyCell2 the darkMoyCell2 to set
	 */
	public void setDarkMoyCell2(double darkMoyCell2) {
		this.darkMoyCell2 = darkMoyCell2;
	}

	/**
	 * @return the incertDarkCell1
	 */
	public double getIncertDarkCell1() {
		return incertDarkCell1;
	}

	/**
	 * @param incertDarkCell1 the incertDarkCell1 to set
	 */
	public void setIncertDarkCell1(double incertDarkCell1) {
		this.incertDarkCell1 = incertDarkCell1;
	}

	/**
	 * @return the incertDarkCell2
	 */
	public double getIncertDarkCell2() {
		return incertDarkCell2;
	}

	/**
	 * @param incertDarkCell2 the incertDarkCell2 to set
	 */
	public void setIncertDarkCell2(double incertDarkCell2) {
		this.incertDarkCell2 = incertDarkCell2;
	}

	/**
	 * @return the posP1Extinction
	 */
	public double getPosP1Extinction() {
		return posP1Extinction;
	}

	/**
	 * @param posP1Extinction the posP1Extinction to set
	 */
	public void setPosP1Extinction(double posP1Extinction) {
		this.posP1Extinction = posP1Extinction;
	}

	/**
	 * @return the posRhombExtinction
	 */
	public double getPosRhombExtinction() {
		return posRhombExtinction;
	}

	/**
	 * @param posRhombExtinction the posRhombExtinction to set
	 */
	public void setPosRhombExtinction(double posRhombExtinction) {
		this.posRhombExtinction = posRhombExtinction;
	}

	/**
	 * @return the posP1
	 */
	public double getPosP1() {
		return posP1;
	}

	/**
	 * @param posP1 the posP1 to set
	 */
	public void setPosP1(double posP1) {
		this.posP1 = posP1;
	}

	/**
	 * @return the nbToursP1
	 */
	public int getNbToursP1() {
		return nbToursP1;
	}

	/**
	 * @param nbToursP1 the nbToursP1 to set
	 */
	public void setNbToursP1(int nbToursP1) {
		this.nbToursP1 = nbToursP1;
	}

	/**
	 * @return the numeroDuTourP1
	 */
	public int getNumeroDuTourP1() {
		return numeroDuTourP1;
	}

	/**
	 * @param numeroDuTourP1 the numeroDuTourP1 to set
	 */
	public void setNumeroDuTourP1(int numeroDuTourP1) {
		this.numeroDuTourP1 = numeroDuTourP1;
	}

	/**
	 * @return the incrementP1
	 */
	public double getIncrementP1() {
		return incrementP1;
	}

	/**
	 * @param incrementP1 the incrementP1 to set
	 */
	public void setIncrementP1(double incrementP1) {
		this.incrementP1 = incrementP1;
	}

	/**
	 * @return the posTranslation
	 */
	public double getPosTranslation() {
		return posTranslation;
	}

	/**
	 * @param posTranslation the posTranslation to set
	 */
	public void setPosTranslation(double posTranslation) {
		this.posTranslation = posTranslation;
	}

	/**
	 * @return the posTranslatHorsFaisceau
	 */
	public double getPosTranslatHorsFaisceau() {
		return posTranslatHorsFaisceau;
	}

	/**
	 * @param posTranslatHorsFaisceau the posTranslatHorsFaisceau to set
	 */
	public void setPosTranslatHorsFaisceau(double posTranslatHorsFaisceau) {
		this.posTranslatHorsFaisceau = posTranslatHorsFaisceau;
	}

	/**
	 * @return the posTranslatDansLeFaisceau
	 */
	public double getPosTranslatDansLeFaisceau() {
		return posTranslatDansLeFaisceau;
	}

	/**
	 * @param posTranslatDansLeFaisceau the posTranslatDansLeFaisceau to set
	 */
	public void setPosTranslatDansLeFaisceau(double posTranslatDansLeFaisceau) {
		this.posTranslatDansLeFaisceau = posTranslatDansLeFaisceau;
	}

	/**
	 * @return the lambda
	 */
	public double getLambda() {
		return lambda;
	}

	/**
	 * @param lambda the lambda to set
	 */
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * @return the filtre
	 */
	public int getFiltre() {
		return filtre;
	}

	/**
	 * @param filtre the filtre to set
	 */
	public void setFiltre(int filtre) {
		this.filtre = filtre;
	}

	/**
	 * @return the lambdaMin
	 */
	public double getLambdaMin() {
		return lambdaMin;
	}

	/**
	 * @param lambdaMin the lambdaMin to set
	 */
	public void setLambdaMin(double lambdaMin) {
		this.lambdaMin = lambdaMin;
	}

	/**
	 * @return the lambdaMax
	 */
	public double getLambdaMax() {
		return lambdaMax;
	}

	/**
	 * @param lambdaMax the lambdaMax to set
	 */
	public void setLambdaMax(double lambdaMax) {
		this.lambdaMax = lambdaMax;
	}

	/**
	 * @return the lambdaPas
	 */
	public double getLambdaPas() {
		return lambdaPas;
	}

	/**
	 * @param lambdaPas the lambdaPas to set
	 */
	public void setLambdaPas(double lambdaPas) {
		this.lambdaPas = lambdaPas;
	}

	/**
	 * @return the pmNbMesures
	 */
	public int getPmNbMesures() {
		return pmNbMesures;
	}

	/**
	 * @param pmNbMesures the pmNbMesures to set
	 */
	public void setPmNbMesures(int pmNbMesures) {
		this.pmNbMesures = pmNbMesures;
	}

	/**
	 * @return the pmMoy
	 */
	public double getPmMoy() {
		return pmMoy;
	}

	/**
	 * @param pmMoy the pmMoy to set
	 */
	public void setPmMoy(double pmMoy) {
		this.pmMoy = pmMoy;
	}

	/**
	 * @return the pmIncert
	 */
	public double getPmIncert() {
		return pmIncert;
	}

	/**
	 * @param pmIncert the pmIncert to set
	 */
	public void setPmIncert(double pmIncert) {
		this.pmIncert = pmIncert;
	}

	/**
	 *
	 */
	public void ouvreObturateur() {
		socketClientDigimetrie.setEcritEcrireTTL(0, true, taLog2);
	}

	/**
	 *
	 */
	public void fermeObturateur() {
		socketClientDigimetrie.setEcritEcrireTTL(0, false, taLog2);
	}

	/**
	 *
	 */
	public void deplaceAbsoluEtAffichePositionDeLaPlatine(int idPlatine, double positionVoulue) {
		//Initialisation de positions des platines
		//Place P1 sur la position d'extinction avec le Wollaston
		socketClientSmc100.setEcritDeplaceAbsolu(idPlatine, positionVoulue, taLog2);

		switch (idPlatine) {
		case 1:
			setPositionMoteurP1(socketClientSmc100.setEcritGetPosition(idPlatine, taLog2));
			System.out.println("Position moteur N° " + idPlatine + " = " + getPositionMoteurP1());
			taLog2.ecrireLigneLog("Position moteur N° " + idPlatine + " = " + getPositionMoteurP1());
			tfPosP1.setText(String.valueOf(getPositionMoteurP1()));
			break;

		case 2:
			setPositionMoteurRhomb(socketClientSmc100.setEcritGetPosition(idPlatine, taLog2));
			System.out.println("Position moteur N° " + idPlatine + " = " + getPositionMoteurRhomb());
			taLog2.ecrireLigneLog("Position moteur N° " + idPlatine + " = " + getPositionMoteurRhomb());
			tfPosRhomb.setText(String.valueOf(getPositionMoteurRhomb()));
			break;

		case 3:
			setPositionMoteurTranslat(socketClientSmc100.setEcritGetPosition(idPlatine, taLog2));
			System.out.println("Position moteur N° " + idPlatine + " = " + getPositionMoteurTranslat());
			taLog2.ecrireLigneLog("Position moteur N° " + idPlatine + " = " + getPositionMoteurTranslat());
			tfPosTranslation.setText(String.valueOf(getPositionMoteurTranslat()));
			break;

		default:
			break;
		}
	}
}
