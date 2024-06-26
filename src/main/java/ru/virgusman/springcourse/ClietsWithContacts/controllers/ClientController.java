package ru.virgusman.springcourse.ClietsWithContacts.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.virgusman.springcourse.ClietsWithContacts.dto.ClientDTO;
import ru.virgusman.springcourse.ClietsWithContacts.dto.ClientResponse;
import ru.virgusman.springcourse.ClietsWithContacts.dto.ContactDTO;
import ru.virgusman.springcourse.ClietsWithContacts.dto.ContactResponse;
import ru.virgusman.springcourse.ClietsWithContacts.errors.ClientNotFoundException;
import ru.virgusman.springcourse.ClietsWithContacts.errors.ContactNotValidException;
import ru.virgusman.springcourse.ClietsWithContacts.errors.ErrorResponse;
import ru.virgusman.springcourse.ClietsWithContacts.errors.NotCreatedException;
import ru.virgusman.springcourse.ClietsWithContacts.services.ClientService;
import ru.virgusman.springcourse.ClietsWithContacts.validators.ClientValidator;

import static ru.virgusman.springcourse.ClietsWithContacts.errors.ErrorUtil.returnErrors;

@RestController
@RequestMapping("/client")
@Tag(name = "Контроллер клиентов", description = "Управление")
public class ClientController {
    private final ClientService clientService;
    private final ClientValidator clientValidator;

    public ClientController(ClientService clientService, ClientValidator clientValidator) {
        this.clientService = clientService;
        this.clientValidator = clientValidator;
    }

    @PostMapping("/add")
    @Operation(summary = "Добавление нового клиента")
    public ResponseEntity<HttpStatus> addClient(@RequestBody @Valid ClientDTO clientDTO,
                                                BindingResult bindingResult) {
        clientValidator.validate(clientDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            returnErrors(bindingResult);
        }
        clientService.save(clientDTO);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Получить список всех клиентов")
    public ClientResponse getAllMeasurement() {
        return new ClientResponse(clientService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Информация клиента по его ID")
    public ClientDTO getClient(@NotNull @PathVariable("id") int id) {
        return clientService.findOneById(id);
    }

    @PostMapping("/{id}/contact")
    @Operation(summary = "Добавление нового контакта пользователю")
    public ResponseEntity<HttpStatus> addContact(@NotNull @PathVariable("id") int id,
                                                 @RequestBody @Valid ContactDTO contactDTO,
                                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            returnErrors(bindingResult);
        }
        clientService.save(contactDTO, id);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/{id}/contacts")
    @Operation(summary = "Все контакты по клиенту")
    public ContactResponse getContactsById(@NotNull @PathVariable("id") int id,
                                           @RequestParam(value = "type", required = false) String type){
        //return clientService.findClientContacts(id, type);
        return new ContactResponse(clientService.findClientContacts(id, type));
    }


    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(NotCreatedException e) {
        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(ClientNotFoundException e) {
        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    private ResponseEntity<ErrorResponse> handleException(ContactNotValidException e) {
        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
