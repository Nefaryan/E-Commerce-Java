package com.nefdev.ecommerce.service;

import com.nefdev.ecommerce.api.modelDTO.LoginDTO;
import com.nefdev.ecommerce.api.modelDTO.RegistrationDTO;
import com.nefdev.ecommerce.dao.UserDAO;
import com.nefdev.ecommerce.dao.VerificationTokenDAO;
import com.nefdev.ecommerce.exception.EmailFailureException;
import com.nefdev.ecommerce.exception.UserAlreadyExistsException;
import com.nefdev.ecommerce.exception.UserNotVerifiedException;
import com.nefdev.ecommerce.model.User;
import com.nefdev.ecommerce.model.VerificationToken;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private UserDAO userDAO; // DAO per la gestione degli utenti nel database
    private VerificationTokenDAO verificationTokenDAO; // DAO per la gestione dei token di verifica nel database
    private EncryptionService encryptionService; // Servizio per la crittografia dei dati sensibili
    private JWTService jwtService; // Servizio per la gestione dei JSON Web Token
    private EmailService emailService; // Servizio per l'invio di email

    /**
     * Costruttore per il servizio utente che riceve le dipendenze necessarie.
     * @param userDAO DAO per gli utenti
     * @param verificationTokenDAO DAO per i token di verifica
     * @param encryptionService Servizio per la crittografia
     * @param jwtService Servizio per i JSON Web Token
     * @param emailService Servizio per l'invio di email
     */
    public UserService(UserDAO userDAO, VerificationTokenDAO verificationTokenDAO, EncryptionService encryptionService,
                       JWTService jwtService, EmailService emailService) {
        this.userDAO = userDAO;
        this.verificationTokenDAO = verificationTokenDAO;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    /**
     * Prova a registrare un utente con le informazioni fornite.
     * @param registrationBody Informazioni per la registrazione
     * @return L'utente registrato salvato nel database
     * @throws UserAlreadyExistsException Lanciata se esiste già un utente con le stesse informazioni
     */
    public User registerUser(RegistrationDTO registrationBody) throws UserAlreadyExistsException, EmailFailureException {
        // Verifica se esiste già un utente con lo stesso indirizzo email o username
        if (userDAO.findByEMailIgnoreCase(registrationBody.getEmail()).isPresent()
                || userDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        // Crea un nuovo utente utilizzando le informazioni fornite
        User user = new User();
        user.setEMail(registrationBody.getEmail());
        user.setUsername(registrationBody.getUsername());
        user.setName(registrationBody.getFirstName());
        user.setSurname(registrationBody.getLastName());
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        // Crea e invia un token di verifica tramite email
        VerificationToken verificationToken = createVerificationToken(user);
        emailService.sendVerificationEmail(verificationToken);
        // Salva l'utente nel database e restituisce l'utente registrato
        return userDAO.save(user);
    }

    /**
     * Crea un oggetto VerificationToken per inviarlo all'utente.
     * @param user L'utente per cui viene generato il token
     * @return L'oggetto VerificationToken creato
     */
    private VerificationToken createVerificationToken(User user) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
    }

    /**
     * Effettua il login di un utente e restituisce un token di autenticazione.
     * @param loginBody Richiesta di login
     * @return Il token di autenticazione. Restituisce Null se la richiesta non è valida
     * @throws UserNotVerifiedException Viene Lanciata se l'utente non è ancora stato verificato
     * @throws EmailFailureException Viene Lanciata se c'è un problema nell'invio dell'email
     */
    public String loginUser(LoginDTO loginBody) throws UserNotVerifiedException, EmailFailureException {
        Optional<User> opUser = userDAO.findByUsernameIgnoreCase(loginBody.getUsername());
        if (opUser.isPresent()) {
            User user = opUser.get();
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                if (user.isEmailVerified()) {
                    // Genera e restituisce un token di autenticazione
                    return jwtService.generateJWT(user);
                } else {
                    // Se l'utente non è verificato, gestisce l'invio di un nuovo token di verifica
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    boolean resend = verificationTokens.isEmpty() ||
                            verificationTokens.get(0).getCreatedTimestamp().before(new Timestamp(System.currentTimeMillis() - (60 * 60 * 1000)));
                    if (resend) {
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenDAO.save(verificationToken);
                        emailService.sendVerificationEmail(verificationToken);
                    }
                    throw new UserNotVerifiedException(resend);
                }
            }
        }
        return null;
    }

    /**
     * Verifica un utente utilizzando il token fornito.
     * @param token Il token da utilizzare per la verifica dell'utente
     * @return True se è stato verificato, false se è già verificato o il token non è valido
     */
    @Transactional
    public boolean verifyUser(String token) {
        Optional<VerificationToken> opToken = verificationTokenDAO.findByToken(token);
        if (opToken.isPresent()) {
            VerificationToken verificationToken = opToken.get();
            User user = verificationToken.getUser();
            if (!user.isEmailVerified()) {
                // Se l'utente non è ancora verificato, imposta lo stato di verifica e aggiorna il database
                user.setEmailVerified(true);
                userDAO.save(user);
                // Cancella il token di verifica utilizzato
                verificationTokenDAO.deleteByUser(user);
                return true;
            }
        }
        return false;
    }

}
