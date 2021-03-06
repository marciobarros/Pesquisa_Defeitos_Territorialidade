package Contributors;

import java.util.Date;

import lombok.Data;

import org.eclipse.egit.github.core.User;

public @Data class Contributors {
	
	private String nome;
	private String login;
	private TipoContributor tipoAjustado;
	private String type;
	private Date dataPrimeiraInteracao;
	private boolean isDeveloper;
	private boolean isReporter;
	
	public Contributors(User user){
		this.nome = user.getName();
		this.login = user.getLogin();
		this.type = user.getType();
		this.tipoAjustado = null;
		this.dataPrimeiraInteracao = null;
		this.isDeveloper = false;
		this.isReporter = false;
	}
	
	public void incluiDataPrimeiraInteracao(Date data, TipoContributor tipo){
		if(this.dataPrimeiraInteracao == null){
			this.dataPrimeiraInteracao = data;
			this.tipoAjustado = tipo;
		}
		else if(this.dataPrimeiraInteracao.after(data)){
			this.dataPrimeiraInteracao = data;
			this.tipoAjustado = tipo;
		}
		
		if(tipo.equals(TipoContributor.REPORTER)){
			this.isReporter = true;
		} else if (tipo.equals(TipoContributor.DEVELOPER)){
			this.isDeveloper = true;
		}
		
	}
}
