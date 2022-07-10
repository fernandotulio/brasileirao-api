package br.com.terreno.brasileiraoapi.util;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import br.com.terreno.brasileiraoapi.dto.PartidaGoogleDTO;
import ch.qos.logback.classic.Logger;
//import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrapingUtil {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ScrapingUtil.class);
	private static final String BASE_URL_GOOGLE = "https://www.google.com/search?q=";
	private static final String COMPLEMENTO_URL_GOOGLE = "&h1=pt-BR";

	
	public static void main(String[] args) {
		 
		//String url = BASE_URL_GOOGLE + "palmeiras+x+corinthians+08/08/2020" + COMPLEMENTO_URL_GOOGLE;
		String url = BASE_URL_GOOGLE + "gremio+v+nautico" + COMPLEMENTO_URL_GOOGLE;
		
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
			
		} catch (IOException e) {
			LOGGER.error("Erro ao tentar conectar no Google com JSOUP -> {}", e.getMessage());
		}
		
		return partida;
	}
	
	public StatusPartida obtemStatusPartida(Document document) {
	
		StatusPartida statusPartida = StatusPartida.PARTIDA_NAO_INICIADA;
		
		boolean isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if(!isTempoPartida){
			String tempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").first().text();
			statusPartida = StatusPartida.PARTIDA_EM_ADAMENTO;
			if(tempoPartida.contains("Pênaltis")) {
				statusPartida = StatusPartida.PARTIDA_PENALTIS;
			}
			LOGGER.info(tempoPartida);
		}
			
		isTempoPartida = document.select("span[class=imso_mh__ft-mtch imso-medium-font imso_mh__ft-mtchc]").isEmpty();
		if (!isTempoPartida) {
			statusPartida = StatusPartida.PARTIDA_ENCERRADA;
		}
		//LOGGER.info(statusPartida.toString());  
		return statusPartida;
	}
	
	
}
