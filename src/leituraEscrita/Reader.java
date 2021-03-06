package leituraEscrita;

import issuesRepositorios.MetodosAuxiliares;
import issuesRepositorios.Repositorio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import marcacoesIssues.AgrupadorMarcacao;
import marcacoesIssues.LabelConsolidado;
import marcacoesIssues.MarcacaoIssue;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;


public class Reader {

	final static String ARQUIVO = "arquivos//listaRepositorios//saidaRepositorios10000.txt";
	final static String ARQUIVOLABELS = "arquivos//Marcacoes Consolidadas//consolidado.csv";
	

	/**
	 * Executa a leitura do arquivo, testando cada uma das tags para identificar 
	 * qual o tipo de informa��o que er� lida.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static ArrayList<Repositorio> executeListaSimples(GitHubClient client, int TAMANHO_AMOSTRA) throws InterruptedException, IOException {
		Scanner file = new Scanner(new BufferedReader(new FileReader(ARQUIVO)));
		ArrayList<Repositorio> repositorios = new ArrayList<Repositorio>();
		RepositoryService repositoryService = new RepositoryService(client);
		IssueService issueService = new IssueService(client);
		int cont = 0;         	
		
		while ((file.hasNext()) && (cont < TAMANHO_AMOSTRA)) {
			String linha = file.nextLine();
			if(!linha.isEmpty()){
				String[] linhaDivida = linha.split(" ");
				Repositorio r = new Repositorio(linhaDivida[2], linhaDivida[9], client); 
				if(!r.getRepositoryName().equals("Vazio")){
					repositorios.add(r);	
				}
				cont++;
				System.out.println("Carregado Repositorio: " + r.getUserName() + "/" + r.getRepositoryName());
			}
		}
		return repositorios;
	}
	
	public static void executeListaComAnalise(GitHubClient client, int TAMANHO_AMOSTRA, int INICIO, ArrayList<LabelConsolidado> consolidadoLabel, ArrayList<MarcacaoIssue> marcacoes) throws InterruptedException, IOException {
		Scanner file = new Scanner(new BufferedReader(new FileReader(ARQUIVO)));
		RepositoryService repositoryService = new RepositoryService(client);
		IssueService issueService = new IssueService(client);
		CommitService commitService = new CommitService(client);
		UserService userService = new UserService(client);
		int cont = 0;         	
		
		while ((file.hasNext()) && (cont < TAMANHO_AMOSTRA)) {
			String linha = file.nextLine();
			if(!linha.isEmpty()){
				if(cont > INICIO){
					String[] linhaDivida = linha.split(" ");
					Repositorio r = new Repositorio(linhaDivida[2], linhaDivida[9], client); 
					if(!r.getRepositoryName().equals("Vazio")){
						//r.downloadCommits(commitService, userService);
						r.calculaQuantidadesIssues(consolidadoLabel);
						//r.defeitosCorrigidosCommitOrigemCSV();
						//r.calculaIssuesFechadosCommit();
						MetodosAuxiliares.analiseMarcacao(consolidadoLabel, marcacoes, r);
						Writer.printConteudoCSV(r, cont);
						Writer.printConteudoRepositorioIssuesCSV(r, cont, client);
						Writer.printAnaliseMarcacaoIssue(r, cont);
						Writer.printContributors(r);	
						System.out.println(cont + " ; Carregado Repositorio: " + r.getUserName() + "/" + r.getRepositoryName());
						
					}
				}
				cont++;
			}
		}
		
		Writer.printAnaliseMarcacaoCompleta(marcacoes);
	}
	
	/*public static ArrayList<Repositorio> executeListaCompleta() throws FileNotFoundException {
		Scanner file = new Scanner(new BufferedReader(new FileReader(ARQUIVOCOMPLETO)));
		ArrayList<Repositorio> repositorios = new ArrayList<Repositorio>();
		while (file.hasNext()) {
			if(file.nextLine().startsWith("_")){
				String[][] linhaDividida = new String[7][10];
				for(int i = 0 ; i < 7 ; i++){
					linhaDividida[i] = file.nextLine().split(" ");
				}	
				Repositorio r = new Repositorio(linhaDividida[0][1], linhaDividida[0][3]); 
				r.setClosedIssue(Integer.parseInt(linhaDividida[2][3]));
				r.setOpenIssue(Integer.parseInt(linhaDividida[2][1]));
				r.setClosedIssueBug(Integer.parseInt(linhaDividida[4][3]));
				r.setOpenIssueBug(Integer.parseInt(linhaDividida[4][1]));
				r.setContadorIssuesCorrigidosCommits(Integer.parseInt(linhaDividida[5][5].replaceAll("[^0-9]", "")));
				r.setContadorIssuesBugCorrigidosCommits(Integer.parseInt(linhaDividida[6][6].replaceAll("[^0-9]", "")));
				repositorios.add(r);
			}
		}
		return repositorios;
	}*/
	
	public static ArrayList<Repositorio> inicializaListaRepositorios(GitHubClient client, int TAMANAHO_LISTA) throws IOException, RequestException, InterruptedException {
		ArrayList<Repositorio> repositorios = new ArrayList<Repositorio>();
		StringBuilder buffer = new StringBuilder();
		RepositoryService repositoryService = new RepositoryService(client);
		IssueService issueService = new IssueService(client);
		CommitService commitService = new CommitService(client);
		int cont = 0;
		
		Map<String, String> params = new HashMap<String, String>();
	    params.put(RepositoryService.FILTER_TYPE, "public");

		Map<String, String> paramsIssues = new HashMap<String, String>();
	    paramsIssues.put(IssueService.FILTER_STATE, "all");
		    
		PageIterator<Repository> iterator = repositoryService.pageAllRepositories();
		
		while(cont < TAMANAHO_LISTA){
			try{	
					Collection<Repository> page = iterator.next();
			    	java.util.Iterator<Repository> itr = page.iterator(); 
		    		while(itr.hasNext()){
				    	Repository repository = itr.next();
				    	if(!repository.isPrivate()){
					    	Repositorio repo = new Repositorio(client, repository.getOwner().getLogin(), repository.getName());
					    	if(!repo.getRepositoryName().equals("Vazio")){
						    	//if(validaRepositorio(repository, repo, issueService, paramsIssues, commitService)){
						    		incluiRepositorio(buffer, repositorios, repo);
									System.out.println(cont);
									cont++;
						    	//}
							}
					    	if((cont % 100) == 0){
					    			Writer.escreveArquivo(criaNome(cont), buffer);
					    			System.out.println("Reposit�rios Inicializados! Lista de " + cont);
					    	}
				    	}						
		    		}
													
			}catch (NoSuchPageException e ){
				//Writer.criaArquivo(criaNome(cont), buffer);
				System.out.println("Reposit�rios Inicializados! M�ximo de Requisi��es Alcan�ada, tentaremos novamente em 10 min");
				Thread.sleep(600 * 1000);
				//return repositorios;		
					
			} catch (RequestException e){
				if(e.getStatus() == 403){
					if(e.getMessage().equals("Repository access blocked (403)")){
						System.out.println("Acesso bloqueado ao Reposit�rio");
					} else {
					System.out.println("Reposit�rios Inicializados! M�ximo de Requisi��es Alcan�ada, tentaremos novamente em 10 min" + " Erro: " + e.getStatus() + "-" + e.getMessage());
					Thread.sleep(600 * 1000);
					}
				}
			}
		}
		return repositorios;
	}
	
	private static boolean validaRepositorio(Repository repository, Repositorio repo, 
			IssueService issueService, Map<String, String> paramsIssues, CommitService commitService){
		if(repository.isPrivate())
			return false;
		
		/*boolean issuesEmpty;
		try {
			issuesEmpty = issueService.getIssues(repo.getRepoId(), paramsIssues).isEmpty();
			if(issuesEmpty)
				return false;
		} catch (IOException e1) {
			return false;
		}*/
	
		int sizeCommit;
		try {
			sizeCommit = commitService.getCommits(repo.getRepoId()).size();
			if(sizeCommit < 2)
				return false;
		} catch (IOException e) {
			return false;
		}	
		return true;
		
	}

	private static void incluiRepositorio(StringBuilder buffer, ArrayList<Repositorio> repositorios, Repositorio repo)
			throws IOException, RequestException {
		repositorios.add(repo);
		Writer.printRepositorios(buffer, repo);
	}
	
	private static String criaNome(int cont){
		String nomeArquivoRepositorios =  "C:/Users/Casimiro/git/Territorialidade/arquivos/listaRepositorios/saidaRepositorios" + cont + ".txt";
		return nomeArquivoRepositorios;
	}
	
	public static String retornaConteudo(File f) throws FileNotFoundException{
		Scanner content = new Scanner(new BufferedReader(new FileReader(f)));
		String contentString = "";
		while(content.hasNext()){
			contentString = contentString + " " +content.nextLine();	
		}
		
		return contentString;
	}
	
	public static ArrayList<LabelConsolidado> geraListaConsolidadaLabels() throws FileNotFoundException{
		Scanner file = new Scanner(new BufferedReader(new FileReader(ARQUIVOLABELS)));
		ArrayList<LabelConsolidado> consolidado = new ArrayList<LabelConsolidado>();
		
		while(file.hasNext()){
			String linha = file.nextLine();
			LabelConsolidado label;
			AgrupadorMarcacao tipo;
			String[] listaPalavras = linha.split(";");
			ArrayList<String> variacoes = new ArrayList<String>();			
			for(int i = 2; i < listaPalavras.length ; i++){
				variacoes.add(listaPalavras[i]);
			}
			tipo = AgrupadorMarcacao.get(listaPalavras[1]);
			label = new LabelConsolidado(listaPalavras[0], tipo, variacoes);
			consolidado.add(label);	
		}
		return consolidado;
	}
	
	public static void consolidaLoc (String arquivo){
		String pastaOrigem = "F:/Saidas";
		File pastaGeral = new File (pastaOrigem);
		File [] pastas = pastaGeral.listFiles();
		for(File p : pastas){
			if(p != null){
				File [] files = p.listFiles();
				for(File f : files){
					if(f.getName() == "loc.txt"){
						int loc = retiraLoc(f);
					}
				}
			}
		}
		
	}

	public static int retiraLoc(File f) {
		int loc = 0;
		int contValue = 0;
		try {
			String texto = retornaConteudo(f);
			int index = texto.indexOf("SUM");
			String [] linhaLoc = texto.substring(index).split("\\W");
			
			for(String l : linhaLoc){
				if(!l.equals("")){
					contValue++;
					if(contValue == 4){
						return Integer.parseInt(l);
					}
				}
			}
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return loc;
	}
	



}
