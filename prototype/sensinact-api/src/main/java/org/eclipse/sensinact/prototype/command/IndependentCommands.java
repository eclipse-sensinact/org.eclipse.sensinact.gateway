package org.eclipse.sensinact.prototype.command;

import java.util.List;
import java.util.stream.Collectors;

import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class IndependentCommands<T> extends AbstractSensinactCommand<List<T>> {

	private List<AbstractSensinactCommand<? extends T>> commands;
	
	private SensinactModel model;
	
	private PromiseFactory pf;
	
	public IndependentCommands(AbstractSensinactCommand<? extends T> command, AbstractSensinactCommand<? extends T> command2) {
		commands = List.of(command, command2);
	}

	public IndependentCommands(AbstractSensinactCommand<? extends T> command, 
			AbstractSensinactCommand<? extends T> command2, AbstractSensinactCommand<? extends T> command3) {
		commands = List.of(command, command2, command3);
	}
	
	public IndependentCommands(List<AbstractSensinactCommand<? extends T>> command) {
		commands = List.copyOf(command);
	}

	@Override
	protected Promise<List<T>> call(SensinactModel model, PromiseFactory promiseFactory) {
		this.model = model;
		this.pf = promiseFactory;
		
		return promiseFactory.all(commands.stream()
			.map(this::safeCall)
			.collect(Collectors.toList()));
	}
	
	@SuppressWarnings("unchecked")
	private Promise<T> safeCall(AbstractSensinactCommand<? extends T> command) {
		return (Promise<T>) safeCall(command, model, pf);
	}
}
