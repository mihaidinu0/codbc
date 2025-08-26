@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepo;
    private final UserRepository userRepo;
    private final CandidateRepository candidateRepo;
    private final EmailService emailService;

    @Transactional
    public CreateRoundResponse createRound(CreateRoundRequest dto) {
        // 1) creatorul trebuie să existe
        MyUser creator = userRepo.findById(dto.getCreatorEmail())
                .orElseThrow(() -> new IllegalArgumentException("Creator not found: " + dto.getCreatorEmail()));

        // 2) runda
        Round round = new Round();
        round.setTitle(dto.getTitle());
        round.setLevel(dto.getLevel());
        round.setCreator(creator);
        round = roundRepo.save(round);

        // 3) normalizăm lista de email-uri
        var emails = dto.getCandidateEmails().stream()
                .filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 4) CREĂM userii candidați (dacă nu există)
        List<MyUser> candidatesUsers = new ArrayList<>();
        for (String email : emails) {
            MyUser u = userRepo.findById(email).orElseGet(() -> {
                MyUser nu = new MyUser();
                nu.setEmail(email);
                nu.setRole("CANDIDATE");
                nu.setActive(true);
                return userRepo.save(nu);
            });
            candidatesUsers.add(u);
        }

        // 5) legăturile Candidate și trimiterea emailurilor
        List<Candidate> links = new ArrayList<>();
        for (MyUser u : candidatesUsers) {
            Candidate c = new Candidate();
            c.setIdCandidate(new CandidateId(u.getEmail(), round.getIdRound()));
            c.setCandidate(u);
            c.setRound(round);
            c.setStatus("INVITED");
            c.setTokenUsed(false);
            links.add(c);
        }
        candidateRepo.saveAll(links);

        for (MyUser u : candidatesUsers) {
            emailService.sendMagicLink(u.getEmail(), round.getIdRound());
        }

        var resp = new CreateRoundResponse();
        resp.setIdRound(round.getIdRound());
        resp.setInvitedCount(candidatesUsers.size());
        resp.setCreatedEmails(new ArrayList<>(emails));
        return resp;
    }
}
