package one.digitalinnovation.gof.service.impl;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import one.digitalinnovation.gof.model.Cliente;
import one.digitalinnovation.gof.model.ClienteRepository;
import one.digitalinnovation.gof.model.Endereco;
import one.digitalinnovation.gof.model.EnderecoRepository;
import one.digitalinnovation.gof.service.ClienteService;
import one.digitalinnovation.gof.service.ViaCepService;

/**
 * Implementação da <b>Strategy</b> {@link ClienteService}, a qual pode ser
 * injetada pelo Spring (via {@link Autowired}). Com isso, como essa classe é um
 * {@link Service}, ela será tratada como um <b>Singleton</b>.
 * 
 * @author falvojr
 */
@Service
public class ClienteServiceImpl implements ClienteService {

	// Singleton: Injetar os componentes do Spring com @Autowired.
	@Autowired
	private ClienteRepository clienteRepository;
	@Autowired
	private EnderecoRepository enderecoRepository;
	@Autowired
	private ViaCepService viaCepService;
	
	// Strategy: Implementar os métodos definidos na interface.
	// Facade: Abstrair integrações com subsistemas, provendo uma interface simples.

	@Override
	public Iterable<Cliente> buscarTodos() {
		return clienteRepository.findAll();
	}

	@Override
	public Cliente buscarPorId(Long id) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("ID de cliente inválido: " + id);
		}

		Optional<Cliente> cliente = clienteRepository.findById(id);
		if (cliente.isPresent()) {
			return cliente.get();
		} else {
			throw new NoSuchElementException("Cliente não encontrado para o ID: " + id);
		}
	}

	@Override
	public void inserir(Cliente cliente) {
		if (cliente == null) {
			throw new IllegalArgumentException("Cliente não pode ser nulo");
		}

		salvarClienteComCep(cliente);
	}

	@Override
	public void atualizar(Long id, Cliente cliente) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("ID de cliente inválido: " + id);
		}

		if (cliente == null) {
			throw new IllegalArgumentException("Cliente não pode ser nulo");
		}

		Optional<Cliente> clienteBd = clienteRepository.findById(id);
		if (clienteBd.isPresent()) {
			salvarClienteComCep(cliente);
		} else {
			throw new NoSuchElementException("Cliente não encontrado para o ID: " + id);
		}
	}

	@Override
	public void deletar(Long id) {
		if (id == null || id <= 0) {
			throw new IllegalArgumentException("ID de cliente inválido: " + id);
		}

		clienteRepository.deleteById(id);
	}

	private void salvarClienteComCep(Cliente cliente) {
		if (cliente == null) {
			throw new IllegalArgumentException("Cliente não pode ser nulo");
		}

		String cep = cliente.getEndereco().getCep();
		if (cep == null || cep.isEmpty()) {
			throw new IllegalArgumentException("CEP inválido: " + cep);
		}

		Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
			Endereco novoEndereco = viaCepService.consultarCep(cep);
			enderecoRepository.save(novoEndereco);
			return novoEndereco;
		});

		cliente.setEndereco(endereco);
		clienteRepository.save(cliente);
	}
}
