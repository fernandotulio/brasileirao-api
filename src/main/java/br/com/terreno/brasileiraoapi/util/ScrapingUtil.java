package br.com.terreno.brasileiraoapi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.com.terreno.brasileiraoapi.dto.PartidaGoogleDTO;
import ch.qos.logback.classic.Logger;
//import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrapingUtil {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE = "https://www.google.com/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&h1=pt-BR";

	private static final String PARTIDA_NAO_INICIADA = "div[class=imso_mh__vs-at-sep imso_mh__team-names-have-regular-font]";
	private static final String JOGO_ROLANDO = "div[class=imso_mh__lv-m-stts-cont]";
	private static final String PARTIDA_ENCERRADA = "span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]";
	private static final String PLACAR_EQUIPE_CASA = "div[class=imso_mh__l-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String PLACAR_EQUIPE_VISITANTE = "div[class=imso_mh__r-tm-sc imso_mh__scr-it imso-light-font]";
	private static final String LOGO_EQUIPE_CASA = "div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String LOGO_EQUIPE_VISITANTE = "div[class=imso_mh__second-tn-ed imso_mh__tnal-cont imso-tnol]";
	private static final String IMG_ITEM_LOGO = "img[class=imso_btl__mh-logo]";
	private static final String GOLS_EQUIPE_CASA = "div[class=imso_gs__tgs imso_gs__left-team]";
	private static final String GOLS_EQUIPE_VISITANTE = "div[class=imso_gs__tgs imso_gs__right-team]";
	private static final String DIV_ITEM_GOLS = "div[class=imso_gs__gs-r]";
	private static final String DIV_PENALIDADES = "div[class=imso_mh_s__psn-sc]";

	
	private static final String HTTPS = "https:";
	private static final String SRC = "src";
	private static final String SPAN = "span";
	private static final String PENALTIS = "Pênaltis";
	private static final String CASA = "CASA";
	private static final String VISITANTE = "VISITANTE";
	
	public static void main(String[] args) {
		 
		//String url = BASE_URL_GOOGLE + "palmeiras+x+corinthians+08/08/2020" + COMPLEMENTO_URL_GOOGLE;
		String url = BASE_URL_GOOGLE + "palmeiras+x+cerro%20porteno+06/07/2022" + COMPLEMENTO_URL_GOOGLE;
		
		ScrapingUtil scraping = new ScrapingUtil();
		scraping.obtemInformacoesPartida(url);
		  
	}

	public PartidaGoogleDTO  obtemInformacoesPartida(String url) {
		PartidaGoogleDTO partida = new PartidaGoogleDTO();
		
		Document document = null;
		
		try {
			
			document = Jsoup.connect(url).get();
			
			String title = document.title();
			LOGGER.info(title, "");
			  
			StatusPartida statusPartida = obtemStatusPartida(document);
			LOGGER.info(statusPartida.toString());  
			//LOGGER.info(StatusPartida.valueOf(title), "");
			 
			if (statusPartida != StatusPartida.PARTIDA_NAO_INICIADA) {
				String tempoPartida = obtemTempoPartida(document);
				LOGGER.info(tempoPartida);
			}
		
			String nomeEquipeCasa = recuperaNomeEquipeCasa(document);
			LOGGER.info(nomeEquipeCasa);
			
			
			//String urlLogoEquipeCasa = recuperaUrlLogoEquipe(document); 
			
			LOGGER.info(recuperaGolsEquipeCasa(document));
			
			
		} catch (IOException e) {
			LOGGER.error("Erro ao tentar conectar no Google com JSOUP -> {}", e.getMessage());
		}
		
		return partida;
	}
	
	public StatusPartida obtemStatusPartida(Document document) {
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		// SITUACOES
		// 1 - Consulta antes do inicio partida
		boolean isTempoPartida = document.select(PARTIDA_NAO_INICIADA).isEmpty();
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		}

		// 2 - jogo rolando ou intervalo
		isTempoPartida = document.select(JOGO_ROLANDO).isEmpty();
		if (!isTempoPartida) {
			String tempoPartida = document.select(JOGO_ROLANDO).first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ANDAMENTO;
			if (tempoPartida.contains(PENALTIS)) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
		}

		// 3 - jogo encerrado
		isTempoPartida = document.select(PARTIDA_ENCERRADA).isEmpty();
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}

		return statusPartida;
	}
	
	public String obtemTempoPartida(Document document) {
		// situações
		// 1 - Consulta antes do inicio partida
		String tempoPartida = null;
		boolean isTempoPartida = document.select(PARTIDA_NAO_INICIADA).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(PARTIDA_NAO_INICIADA).first().text();
		}

		// 2 - jogo rolando ou intervalo
		isTempoPartida = document.select(JOGO_ROLANDO).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(JOGO_ROLANDO).first().text();
		}

		// 3 - jogo encerrado
		isTempoPartida = document.select(PARTIDA_ENCERRADA).isEmpty();
		if (!isTempoPartida) {
			tempoPartida = document.select(PARTIDA_ENCERRADA).first().text();
		}

		return tempoPartida;//corrigeTempoPartida(tempoPartida);
	}
	
	private static String corrigeTempoPartida(String tempo) {
		String tempoPartida = "";
		if (tempo.contains("'")) {
			tempoPartida = tempo.replace(" ", "");
			tempoPartida = tempoPartida.replace("'", "").concat(" min");
		} else {
			if (tempo.contains("+")) {
				tempoPartida = tempo.replace(" ", "").concat(" min");
			} else {
				return tempo;
			}
		}
		return tempoPartida;
	}
	
	
	public String recuperaNomeEquipeCasa(Document document) {
		Element elemento = document.selectFirst("div[class=imso_mh__first-tn-ed imso_mh__tnal-cont imso-tnol]");
		String nomeEquipe = elemento.select("span").text();
		return nomeEquipe;
	}
	
	public String recuperaUrlLogoEquipe(Document document, String itemHtml) {
		Element elementLogoEquipe = document.select(itemHtml).first();
		String urlLogo = HTTPS + elementLogoEquipe.select(IMG_ITEM_LOGO).attr(SRC);

		return urlLogo;
	}
	
	
	public String recuperaGolsEquipeCasa(Document document) {
		
		List<String> golsEquipe = new ArrayList<>();

		Elements timeCasa = document.select("div[class=imso_gs__tgs imso_gs__left-team]").select(DIV_ITEM_GOLS);
		
		//LOGGER.info(timeCasa.text()); 
		
		for (Element e : timeCasa) {
			String infoGol = e.select(DIV_ITEM_GOLS).text();
			
			golsEquipe.add(infoGol);
		}

		return golsEquipe.isEmpty() ? null : String.join(", ", golsEquipe);
	}
	
	
	
}
